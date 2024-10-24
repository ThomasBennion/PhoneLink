#version 330 core
in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2DRect texture1;

void main() {
    FragColor = texture(texture1, TexCoord);
}

