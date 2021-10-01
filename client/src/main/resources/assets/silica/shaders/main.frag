#version 330

in vec2 outTexCoord;

out vec4 outColor;

uniform sampler2D diffuseMap;

void main() {
    outColor = texture(diffuseMap, outTexCoord);
}