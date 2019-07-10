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
float dividMod = 0.40f;

float map (float value, float minV, float maxV, float newMin, float newMax){
    float perc = (value - minV) / (maxV - minV);
    float val = perc * (newMax - newMin) + newMin;
    return val;
}

void main() {
    //    vec4 finalColor = vertColor * distance(gl_Position.xy, vec2(0.5, 0.5));
    float ang = map(vertTexCoord.y, 0, 1, -PI * piMult, PI * piMult);
    vec2 pos = vec2( mouseOffset.x / atan(mouseOffset.y, vertTexCoord.y + cos(ang * time) / trans), mouseOffset.y / atan(mouseOffset.x, vertTexCoord.x - sin(ang * time) / offset));
    vec2 wavelines = vec2(vertTexCoord.x  - (sin(ang + time / vertTexCoord.x) / (offset * trans * 100)), vertTexCoord.y * cos(ang * time / 10) * distance(vertTexCoord.xy, vec2(mouseOffset.x - offset, mouseOffset.y - trans)));
    vec4 secondColor = texture2D(texture, (wavelines - vec2(offset, trans)) * zoomAmount);
    vec4 mixColor = texture2D(previousFrame, (pos + (wavelines / fract(zoomAmount)) * zoomAmount));
    mixColor *= (trans / dividMod);
    mixColor *= offset / dividMod;
    secondColor.b += sin(offset * time);
    if (max(max(secondColor.r, secondColor.b), secondColor.g) < 0.093) {
        color = vec4(mixColor.rgb, (distance(pos, mouseOffset)) * (trans / 2));
    } else if (min(min(mixColor.r, mixColor.g), mixColor.b) > 0.57) {
        color = vec4(secondColor.rgb, (distance(wavelines, mouseOffset)) * (trans / 3));
    } else {
        color = vec4(vec3(mix(mixColor.rgb, secondColor.rgb, 0.5)), distance(vertTexCoord.xy, mouseOffset.xy) * trans);
    }
//    color = mixColor;
}
