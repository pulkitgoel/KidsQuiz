"""Generate chiptune-style sound effects for KidQuiz into app/src/main/res/raw/.

Run:  python scripts/generate_sfx.py
Requires: numpy (pip install numpy)

The generated WAVs are checked into the repo, so the Android build never
depends on Python. Re-run only when tweaking a sound.
"""
import math
import os
import wave

import numpy as np

SAMPLE_RATE = 44100
OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")

# Note frequencies (Hz)
NOTES = {
    "C4": 261.63, "D4": 293.66, "E4": 329.63, "F4": 349.23, "G4": 392.00,
    "A4": 440.00, "B4": 493.88,
    "C5": 523.25, "D5": 587.33, "E5": 659.25, "F5": 698.46, "G5": 783.99,
    "A5": 880.00, "B5": 987.77, "C6": 1046.50, "E6": 1318.51, "G6": 1567.98,
    "A3": 220.00, "G3": 196.00, "E3": 164.81, "C3": 130.81, "D3": 146.83,
}


def t_axis(duration):
    return np.linspace(0, duration, int(SAMPLE_RATE * duration), endpoint=False)


def osc(freq, duration, shape="square", bend=0.0):
    """Oscillator. bend = fractional pitch drift over the duration (e.g. -0.2 slides down 20%)."""
    t = t_axis(duration)
    if bend:
        inst_freq = freq * (1.0 + bend * t / duration)
        phase = 2 * np.pi * np.cumsum(inst_freq) / SAMPLE_RATE
    else:
        phase = 2 * np.pi * freq * t
    if shape == "square":
        return np.sign(np.sin(phase)) * 0.6
    if shape == "triangle":
        return (2 / np.pi) * np.arcsin(np.sin(phase))
    if shape == "saw":
        return 2 * ((phase / (2 * np.pi)) % 1.0) - 1.0
    return np.sin(phase)


def adsr(signal, attack=0.005, decay=0.03, sustain=0.7, release=0.05):
    n = len(signal)
    env = np.ones(n) * sustain
    a = min(int(attack * SAMPLE_RATE), n)
    d = min(int(decay * SAMPLE_RATE), max(n - a, 0))
    r = min(int(release * SAMPLE_RATE), n)
    if a:
        env[:a] = np.linspace(0, 1, a)
    if d:
        env[a:a + d] = np.linspace(1, sustain, d)
    if r:
        env[n - r:] = env[n - r:] * np.linspace(1, 0, r)
    return signal * env


def tone(note, duration, shape="square", bend=0.0, **env):
    freq = NOTES[note] if isinstance(note, str) else note
    return adsr(osc(freq, duration, shape, bend), **env)


def silence(duration):
    return np.zeros(int(SAMPLE_RATE * duration))


def noise_burst(duration, lowpass=0.3):
    n = int(SAMPLE_RATE * duration)
    noise = np.random.default_rng(42).uniform(-1, 1, n)
    # crude one-pole lowpass to soften the hiss
    out = np.empty(n)
    acc = 0.0
    for i in range(n):
        acc += lowpass * (noise[i] - acc)
        out[i] = acc
    return adsr(out, attack=0.001, decay=0.01, sustain=0.6, release=duration * 0.6)


def mix(*layers):
    n = max(len(x) for x in layers)
    out = np.zeros(n)
    for x in layers:
        out[:len(x)] += x
    return out


def gliss(f_start, f_end, duration, shape="triangle"):
    t = t_axis(duration)
    inst = np.geomspace(f_start, f_end, len(t))
    phase = 2 * np.pi * np.cumsum(inst) / SAMPLE_RATE
    if shape == "square":
        sig = np.sign(np.sin(phase)) * 0.6
    else:
        sig = (2 / np.pi) * np.arcsin(np.sin(phase))
    return adsr(sig, attack=0.01, decay=0.05, sustain=0.8, release=0.08)


def write_wav(name, signal, gain=0.8):
    signal = signal / max(1e-9, np.max(np.abs(signal))) * gain
    pcm = (signal * 32767).astype(np.int16)
    path = os.path.join(OUT_DIR, name)
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SAMPLE_RATE)
        w.writeframes(pcm.tobytes())
    print(f"wrote {path} ({len(pcm) / SAMPLE_RATE:.2f}s)")


def main():
    os.makedirs(OUT_DIR, exist_ok=True)

    # Tap: tiny bright blip
    write_wav("sfx_tap.wav", tone(880, 0.03, "square", release=0.015), gain=0.5)

    # Correct: ascending major arpeggio C5-E5-G5
    write_wav("sfx_correct.wav", np.concatenate([
        tone("C5", 0.09), tone("E5", 0.09), tone("G5", 0.12, release=0.08),
    ]))

    # Wrong: low sawtooth double-buzz with downward bend
    buzz = tone(150, 0.12, "saw", bend=-0.25, sustain=0.9)
    write_wav("sfx_wrong.wav", np.concatenate([buzz, silence(0.04), buzz]), gain=0.7)

    # Combo (x3): 4-note ascending run ending on the octave
    write_wav("sfx_combo.wav", np.concatenate([
        tone("C5", 0.08, "triangle"), tone("E5", 0.08, "triangle"),
        tone("G5", 0.08, "triangle"), tone("C6", 0.14, "triangle", release=0.1),
    ]))

    # On fire (x5): rapid glissando + sparkle layer
    sparkle = np.concatenate([
        tone("C6", 0.05, "square"), tone("E6", 0.05, "square"), tone("G6", 0.08, "square"),
    ]) * 0.4
    body = gliss(NOTES["C5"], NOTES["C6"], 0.35)
    write_wav("sfx_on_fire.wav", mix(body, np.concatenate([silence(0.3), sparkle])))

    # Fanfare: I-IV-V-I chiptune chords, two voices + noise crash
    def chord(roots, duration):
        return mix(*[tone(r, duration, "square") * 0.5 for r in roots])

    fanfare = np.concatenate([
        chord(["C5", "E5", "G5"], 0.22),
        chord(["F4", "A4", "C5"], 0.22),
        chord(["G4", "B4", "D5"], 0.22),
        mix(chord(["C5", "E5", "G5", "C6"], 0.5), noise_burst(0.25) * 0.3),
    ])
    write_wav("sfx_fanfare.wav", fanfare)

    # Tick: 15ms click for score roll-up
    write_wav("sfx_tick.wav", tone(1200, 0.015, "sine", attack=0.001, release=0.008), gain=0.45)

    # Heart break: descending minor pair + noise crack
    write_wav("sfx_heart_break.wav", mix(
        np.concatenate([tone("E4", 0.13, "triangle"), tone("C4", 0.18, "triangle", release=0.12)]),
        noise_burst(0.06) * 0.5,
    ), gain=0.7)

    # Milestone: shimmering upward gliss + sustained major chord
    write_wav("sfx_milestone.wav", np.concatenate([
        gliss(NOTES["C5"], NOTES["C6"], 0.4),
        mix(*[tone(n, 0.6, "triangle", release=0.35) * 0.5 for n in ("C5", "E5", "G5", "C6")]),
    ]))

    # Rescue: gentle hopeful up-chirp for the mascot rescue dialog
    write_wav("sfx_rescue.wav", np.concatenate([
        tone("G4", 0.1, "triangle"), tone("C5", 0.16, "triangle", release=0.1),
    ]), gain=0.6)


if __name__ == "__main__":
    main()
