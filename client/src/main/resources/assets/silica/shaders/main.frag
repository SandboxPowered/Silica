#version 330

in vec2 outTexCoord;
in vec3 outNormal;
in mat3 TBN;

out vec4 outColor;

uniform sampler2D diffuseMap;
uniform sampler2D normalMap;

uniform vec3 sunDir;

void main() {
    vec3 lightDir = TBN * sunDir;

    vec4 texColor = texture(diffuseMap, outTexCoord);
    if (texColor.a < 0.05) discard;

    vec3 normal = texture(normalMap, outTexCoord).rgb;
    normal = normal * 2.0 - 1.0;
    normal = normalize(TBN * normal);

    float diff = max(dot(lightDir, normal), 0.0);
    vec3 diffuse = diff * texColor.rgb;

    vec3 ambient = (0.1 * texColor).rgb;

    outColor = vec4(normal, texColor.a);
}