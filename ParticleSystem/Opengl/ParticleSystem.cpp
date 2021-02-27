#include "ParticleSystem.h"

#include <labhelper.h>
#include <algorithm>
#include <stb_image.h>

void ParticleSystem::kill(int id)
{
	particles[id] = particles.back();
	particles.pop_back();
}

void ParticleSystem::spawn(Particle particle)
{
	if (particles.size() >= max_size) return;

	const float theta = labhelper::uniform_randf(0.f, 2.f * M_PI);

	// For a uniform distribution on a unit sphere
	//const float u = labhelper::uniform_randf(-1.f, 1.f);

	// For a fountain shape
	const float u = labhelper::uniform_randf(0.95f, 1.f);

	// For a Vertical fountain
	//glm::vec3 pos = glm::vec3(sqrt(1.f - u * u) * cosf(theta), u, sqrt(1.f - u * u) * sinf(theta));

	// For a Horizontal fountain
	glm::vec4 exhaustVelocity = glm::vec4(
		u,
		sqrt(1.f - u * u) * cosf(theta),
		sqrt(1.f - u * u) * sinf(theta),
		0.0f);

	// Rotate exhaust velocity according to current rotation of the model.
	exhaustVelocity = spawnOriginMatrix * exhaustVelocity;

	particle.velocity = exhaustVelocity * 10.0f;

	// particles spawn at the "origin" which is the fighter models position.
	particle.pos = spawnOriginMatrix * glm::vec4(1);

	particle.lifetime = 0.0f;
	particle.life_length = life_length;

	particles.push_back(particle);
}

void ParticleSystem::process_particles(float dt)
{
	for (size_t i = 0; i < particles.size(); i++) {
		if (particles[i].lifetime < particles[i].life_length) continue;

		kill(i);
		i--;
	}

	for (size_t i = 0; i < particles.size(); i++) {
		particles[i].lifetime += dt;
		particles[i].pos += particles[i].velocity * dt;
	}

	timeLapsed += dt;
	//if (timeLapsed < 1.0f) return;

	for (size_t i = 0; i < particlesPerFrame; i++) {
		Particle particle{};
		spawn(particle);
	}
}

void ParticleSystem::init_visuals()
{
	glGenBuffers(1, &particlesPositionBuffer);
	glBindBuffer(GL_ARRAY_BUFFER, particlesPositionBuffer);
	glBufferData(GL_ARRAY_BUFFER, sizeof(glm::vec4) * max_size, nullptr, GL_STATIC_DRAW);

	glGenVertexArrays(1, &particlesVAO);
	glBindVertexArray(particlesVAO);
	glBindBuffer(GL_ARRAY_BUFFER, particlesPositionBuffer);
	glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, nullptr);
	glEnableVertexAttribArray(0);
	glEnableVertexAttribArray(1);

	int w, h, comp;
	unsigned char* image = stbi_load("../scenes/explosion.png", &w, &h, &comp, STBI_rgb_alpha);

	glGenTextures(1, &particlesTexture);
	glBindTexture(GL_TEXTURE_2D, particlesTexture);
	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
	free(image);

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
}

void ParticleSystem::extractUploadData(const glm::mat4& viewMatrix, const glm::mat4& projectionMatrix)
{
	std::vector<glm::vec4> data;

	/* Code for extracting data goes here */
	for (size_t i = 0; i < particles.size(); i++) {
		glm::vec4 positionAndLifetime = viewMatrix * glm::vec4(particles[i].pos, 1.0f);
		positionAndLifetime.w = particles[i].lifetime / particles[i].life_length;
		data.push_back(positionAndLifetime);
	}

	// sort particles with sort from c++ standard library
	std::sort(data.begin(), std::next(data.begin(), data.size()),
		[](const glm::vec4& lhs, const glm::vec4& rhs) { return lhs.z < rhs.z; });

	glBindBuffer(GL_ARRAY_BUFFER, particlesPositionBuffer);
	glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(glm::vec4) * data.size(), &data[0]);
}

void ParticleSystem::draw_visuals(GLuint particleShader, const glm::mat4& viewMatrix, const glm::mat4& projectionMatrix,
	float windowWidth, float windowHeight)
{
	// Required to safeguard GPU from receiving and attempting to read an empty vector
	// Without this safeguard the program crashes when no particles are alive.
	if (particles.size() <= 0) return;

	extractUploadData(viewMatrix, projectionMatrix);

	glUseProgram(particleShader);

	labhelper::setUniformSlow(particleShader, "screen_x", windowWidth);
	labhelper::setUniformSlow(particleShader, "screen_y", windowHeight);
	labhelper::setUniformSlow(particleShader, "P", projectionMatrix);

	glActiveTexture(GL_TEXTURE0);
	glBindTexture(GL_TEXTURE_2D, particlesTexture);

	// Enable shader program point size modulation.
	glEnable(GL_PROGRAM_POINT_SIZE);
	// Enable blending.
	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	glBindVertexArray(particlesVAO);
	glDrawArrays(GL_POINTS, 0, particles.size());
}
