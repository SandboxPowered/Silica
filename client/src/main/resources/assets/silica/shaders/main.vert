#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
out vec2 outTexCoord;

out vec3 vertPos;

void main() {
    gl_Position = projection * view * vec4(position, 1.0);
    vertPos = position;
    outTexCoord = texCoord;
}