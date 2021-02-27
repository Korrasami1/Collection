#version 420

// required by GLSL spec Sect 4.5.3 (though nvidia does not, amd does)
precision highp float;

///////////////////////////////////////////////////////////////////////////////
// Material
///////////////////////////////////////////////////////////////////////////////
uniform vec3 material_color;
uniform float material_reflectivity;
uniform float material_metalness;
uniform float material_fresnel;
uniform float material_shininess;
uniform float material_emission;

uniform int has_emission_texture;
uniform int has_color_texture;
layout(binding = 0) uniform sampler2D colorMap;
layout(binding = 5) uniform sampler2D emissiveMap;

///////////////////////////////////////////////////////////////////////////////
// Environment
///////////////////////////////////////////////////////////////////////////////
layout(binding = 6) uniform sampler2D environmentMap;
layout(binding = 7) uniform sampler2D irradianceMap;
layout(binding = 8) uniform sampler2D reflectionMap;
uniform float environment_multiplier;

///////////////////////////////////////////////////////////////////////////////
// Light source
///////////////////////////////////////////////////////////////////////////////
uniform vec3 point_light_color = vec3(1.0, 1.0, 1.0);
uniform float point_light_intensity_multiplier = 50.0;

///////////////////////////////////////////////////////////////////////////////
// Shadow Mapping
///////////////////////////////////////////////////////////////////////////////
in vec4 shadowMapCoord;
layout(binding = 10) uniform sampler2DShadow shadowMapTex;

///////////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////////
#define PI 3.14159265359

///////////////////////////////////////////////////////////////////////////////
// Input varyings from vertex shader
///////////////////////////////////////////////////////////////////////////////
in vec2 texCoord;
in vec3 viewSpaceNormal;
in vec3 viewSpacePosition;

///////////////////////////////////////////////////////////////////////////////
// Input uniform variables
///////////////////////////////////////////////////////////////////////////////
uniform mat4 viewInverse;
uniform vec3 viewSpaceLightPosition;
uniform vec3 viewSpaceLightDir;
uniform float spotOuterAngle;
uniform float spotInnerAngle;

///////////////////////////////////////////////////////////////////////////////
// Output color
///////////////////////////////////////////////////////////////////////////////
layout(location = 0) out vec4 fragmentColor;



vec3 calculateDirectIllumiunation(vec3 wo, vec3 n)
{
	vec3 w1 = normalize(viewSpaceLightPosition - viewSpacePosition);
	float d = length(viewSpaceLightPosition - viewSpacePosition);

	vec3 Li = point_light_intensity_multiplier * point_light_color * (1.0f/(d*d));

	if(dot(n,w1) <= 0.0f){
		return vec3(0.0f);
	}

	vec3 diffuse_term = material_color * (1.0f/PI) * abs(dot(n,w1)) * Li;

	vec3 w0 = normalize(-viewSpacePosition);
	vec3 wi = normalize(viewSpaceLightPosition - viewSpacePosition);
	vec3 wh = normalize(wi+w0);
	float F = material_fresnel + (1 - material_fresnel) * pow((1 - dot(wh, wi)),5);
	float s = material_shininess;
	float D = (s + 2) / (2*PI) * pow(abs(dot(n,wh)),s);
	float G = min(1, min(2*(dot(n, wh)*dot(n, w0)/dot(w0, wh)), 2*(dot(n, wh)*dot(n, wi))/dot(w0, wh)));	
	float brdf = F*D*G / (4*dot(n,w0)*dot(n,wi));

	float m = material_metalness;
	float r = material_reflectivity;

	vec3 dielectic_term = brdf * dot(n,wi) * Li + (1 - F) * diffuse_term;
	vec3 metal_term = brdf * material_color * dot(n,wi) * Li;
	vec3 microfacet_term = m * metal_term + (1-m) * dielectic_term;
	return r * microfacet_term + (1-r) * diffuse_term;
}

vec3 calculateIndirectIllumination(vec3 wo, vec3 n)
{
	vec3 nws = normalize(mat3(viewInverse) * n);

	// Calculate the spherical coordinates of the direction
	float theta = acos(max(-1.0f, min(1.0f, nws.y)));
	float phi = atan(nws.z, nws.x);
	if(phi < 0.0f)
	{
		phi = phi + 2.0f * PI;
	}

	vec2 lookup = vec2(phi / (2.0 * PI), theta / PI);
	fragmentColor = environment_multiplier * texture(irradianceMap, lookup);
	
	vec3 irradiance = vec3(fragmentColor);
	vec3 diffuse_term = material_color * (1.0f/PI) * irradiance;

	vec3 wi = reflect(-wo, n);
	vec3 wiws = normalize(mat3(viewInverse) * wi);
	float s = material_shininess;
	float roughness = sqrt(sqrt(2/(s+2)));
	
	float theta2 = acos(max(-1.0f, min(1.0f, wiws.y)));
	float phi2 = atan(wiws.z, wiws.x);
	if(phi2 < 0.0f)
	{
		phi2 = phi2 + 2.0f * PI;
	}

	vec2 lookup2 = vec2(phi2 / (2.0 * PI), theta2 / PI);

	vec3 Li = environment_multiplier * textureLod(reflectionMap, lookup2, roughness * 7.0f).xyz;
	
	vec3 w0 = normalize(-viewSpacePosition);
	vec3 wh = normalize(wiws+w0);
	float F = material_fresnel + (1 - material_fresnel) * pow((1 - dot(wh, wiws)),5);
	vec3 dielectic_term = F * Li + (1 - F) * diffuse_term;
	vec3 metal_term = F * material_color * Li;

	float m = material_metalness;
	float r = material_reflectivity;
	vec3 microfacet_term = m * metal_term + (1-m) * dielectic_term;

	return r * microfacet_term + (1-r) * diffuse_term;
}

void main()
{
	float visibility = 1.0;
	float attenuation = 1.0;

	//float depth= texture( shadowMapTex, shadowMapCoord.xy/shadowMapCoord.w ).r;
	//float visibility= (depth>=(shadowMapCoord.z/shadowMapCoord.w)) ? 1.0 : 0.0;
	visibility = textureProj( shadowMapTex, shadowMapCoord );

	vec3 posToLight = normalize(viewSpaceLightPosition - viewSpacePosition);
	float cosAngle = dot(posToLight, -viewSpaceLightDir);

	// Spotlight with hard border:
	//float spotAttenuation = (cosAngle > spotOuterAngle) ? 1.0 : 0.0;
	float spotAttenuation = smoothstep(spotOuterAngle, spotInnerAngle, cosAngle);
	visibility *= spotAttenuation;

	vec3 wo = -normalize(viewSpacePosition);
	vec3 n = normalize(viewSpaceNormal);

	// Direct illumination
	vec3 direct_illumination_term = visibility * calculateDirectIllumiunation(wo, n);

	// Indirect illumination
	vec3 indirect_illumination_term = calculateIndirectIllumination(wo, n);

	///////////////////////////////////////////////////////////////////////////
	// Add emissive term. If emissive texture exists, sample this term.
	///////////////////////////////////////////////////////////////////////////
	vec3 emission_term = material_emission * material_color;
	if(has_emission_texture == 1)
	{
		emission_term = texture(emissiveMap, texCoord).xyz;
	}

	vec3 shading = direct_illumination_term + indirect_illumination_term + emission_term;

	fragmentColor = vec4(shading, 1.0);
	return;
}
