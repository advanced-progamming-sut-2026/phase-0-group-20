#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec4 u_tintColor;
uniform float u_damageFlash;

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);

    if(texColor.a == 0.0) {
        gl_FragColor = texColor;
        return;
    }

    vec3 finalColor = mix(texColor.rgb, vec3(1.0, 1.0, 1.0), u_damageFlash * 0.7);
    finalColor = mix(finalColor, u_tintColor.rgb, u_tintColor.a);

    gl_FragColor = vec4(finalColor, texColor.a) * v_color;
}
