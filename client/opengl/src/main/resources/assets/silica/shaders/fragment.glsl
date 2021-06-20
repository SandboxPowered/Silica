#version 450

layout(location = 0) in vec3 exColour;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = vec4(exColour, 1.0);
}