#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec3 normal;
layout(location = 3) in vec3 tangent;
layout(location = 4) in vec3 bitangent;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
out vec2 outTexCoord;

out vec3 outNormal;
out vec3 vertPos;
out mat3 TBN;

void main() {
    gl_Position = projection * view * vec4(position, 1.0);
    vertPos = position;
    outTexCoord = texCoord;
    outNormal = normal;

    vec3 T = normalize(vec3(model * vec4(tangent, 0.0)));
    vec3 B = normalize(vec3(model * vec4(bitangent, 0.0)));
    vec3 N = normalize(vec3(model * vec4(normal, 0.0)));
    TBN = transpose(mat3(T, B, N));
}