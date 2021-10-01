#version 330

in vec2 outTexCoord;

out vec4 outColor;

uniform sampler2D diffuseMap;

void main() {
    vec4 texColor = texture(diffuseMap, outTexCoord);
    if (texColor.a < 0.05) discard;
    outColor = texColor;
}