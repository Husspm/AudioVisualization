#version 430

    #define PI 3.1415926535897932384626433832795

uniform float greenVal;
uniform float redVal;
uniform sampler2D texture;
uniform sampler2D previousFrame;
in vec4 vertColor;
in vec4 vertTexCoord;
uniform float offset;
uniform vec2 mouseOffset;
uniform float time;
uniform float trans;
uniform float zoomAmount;
uniform int piMult;
out vec4 color;
float dividMod = 0.80f;

float map (float value, float minV, float maxV, float newMin, float newMax){
    float perc = (value - minV) / (maxV - minV);
    float val = perc * (newMax - newMin) + newMin;
    return val;
}

void main() {
    //    vec4 finalColor = vertColor * distance(gl_Position.xy, vec2(0.5, 0.5));
    vec4 color_1 = texture2D(texture, vertTexCoord.xy);
    vec4 color_2 = texture2D(previousFrame, vertTexCoord.xy);
    color = vec4(vec3(mix(color_1.rgb, color_2.rgb, trans)), 0.61);
    //    color = vec4(1, 0, 0, 1);
    //        color = secondColor;
}