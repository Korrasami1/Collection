#pragma once
//this is part of a lab project as a pair of people, both coded their own solution and collaborated on optimization

#include <GL/glew.h>
#include <vector>
#include <glm/detail/type_vec3.hpp>
#include <glm/mat4x4.hpp>

struct Particle
{
	float lifetime;
	float life_length;
	glm::vec3 velocity;
	glm::vec3 pos;
};

class ParticleSystem
{
public:
	// Members
	std::vector<Particle> particles;
	int max_size;
	glm::mat4 spawnOriginMatrix = glm::mat4(0);

	// Ctor/Dtor
	ParticleSystem() : max_size(0)
	{
	}
	explicit ParticleSystem(int size) : max_size(size)
	{
	}
	~ParticleSystem()
	{
	}
	// Methods
	void kill(int id);
	void spawn(Particle particle);
	void process_particles(float dt);

	void init_visuals();
	void extractUploadData(const glm::mat4& viewMatrix, const glm::mat4& projectionMatrix);
	void draw_visuals(GLuint particleShader, const glm::mat4& viewMatrix, const glm::mat4& projectionMatrix, float windowWidth, float windowHeight);

private:
	GLuint particlesVAO;
	GLuint particlesTexture;
	GLuint particlesPositionBuffer;

	int particlesPerFrame = 64;
	float timeLapsed = 0.0f;
	float life_length = 5.0f;
};
