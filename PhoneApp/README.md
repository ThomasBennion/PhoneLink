# Rubatone
Rubatone is an Android mobile application, combined with a wearable application, built for WearOS devices. Rubatone is programmed in Kotlin.

Rubatone creates music based on your biometric data inputs. The wearable sends sensor data to the phone app, which is used to create or change the music.

One way of generating music involves moving your arm around to change accelerometer and gyrometer values. These 'active' inputs are used to change the sound of a melody instrument. The accelerometer measures the force behind your arm movements, making louder sounds for more forceful movements and quieter sounds for gentler movements. The gyrometer changes the pitch based on the rotation of your arm - pointing it directly upwards creates a high pitch, and pointing it towards the ground creates a low pitch. Rotating your arm up or down will increase or decrease the pitch.

Another way to create music uses 'passive' inputs (heart rate, light intensity of the environment, and ambient temperature). These inputs are used to directly change other musical components (drums, bass, and chords) that are used as an 'instrumental' section to accompany your 'active' inputs!

Rubatone is designed to be more accessible to users with a limited or no musical background, with unique gestural and passive control schemes.

Have fun trying out the Rubatone, and don't be afraid to break musical boundaries!


# Marking Criteria
## Ethical concerns, security, and privacy
Some additional information on these issues has been included as header comments in both the mobile app file (*./android-companion-app/app/src/main/java/com/example/Heartbeats_by_Dr_Dre/MainActivity.kt*) and wearable app file (*./android-companion-app/wearosapp/src/main/java/com/example/Heartbeats_by_Dr_Dre/MainActivity.kt*).

As well as this, there is some information included in the Pure Data patch in *./pd_patches/passive_inputs_test.pd* as comments on the main page. I will add relevant comments below to make it easier to find this information if required:
```
The mobile app code sends sensor data as float values to this patch. When input values are received, some processing is performed on the inputs - they are bounded to particular ranges that would be feasible for users to recreate in non-dangerous environments and contexts. eg. There is an upper limit on the physical force with which a user can move their arm (after which, values are treated the same as the upper bound) - this will disincentivise users from performing dangerous arm movements.

Passive inputs are used to create/generate percussion/drums, chords/pads and bass instruments. Heart rate data is used directly to change the tempo of the music - faster heart rate gives a faster tempo. There are also several intensity thresholds for the tempo, which change the loops chosen by the drums/percussion section (to try and match the intensity levels associated with that heart rate). For heart rate values from 160 BPM upwards, this tempo threshold is mapped to the 'lowest intensity' tempo state, which contains minimal or no drums/percussion. We hope that this will discourage users from trying to increase their heart rate into unsafe/dangerously high ranges.

Temperature and light intensity data are mapped into discrete state spaces, which change the music into distinct sounding 'sections' when these values change past certain thresholds. As with all other values, these have been bounded to ranges which were considered easily reproducible (such that a user wouldn't endanger themself). 
```
**Some examples of this (screenshots):**

*Bounding of input values for heart rate (1):* https://drive.google.com/file/d/1zINz8bfcIwN-rqmBBclnKzvAVfgSi5OU/view?usp=share_link

*Bounding of input values for heart rate (2):* https://drive.google.com/file/d/1FOCVuVCRSluvm8hGfcozWtu2jaiybaA4/view?usp=share_link

*Heart rate intensity thresholds:* https://drive.google.com/file/d/10LlM_LDnamRXnLhZY6Fzz9J11v9PD9lv/view?usp=share_link

```
A note about the privacy/security:

The audio synthesis component is designed to receive a single value of data for each sensor. When new data is received, the previously received data is then immediately overwritten. This patch was implemented so that a user's data would not be stored, beyond the most recently received value. As well as this, if the patch receives values of 999.0 from a sensor (indicating that the sensor has been turned off), the values currently held within the patch should be reset to the default start-up values, with the user's data being overwritten.

Some sensors (heart rate, temperature, light intensity) do not alter the input values beyond bounding the input values to a sensible range. The active input sensor (accelerometer and gyrometer) values will be transformed/rescaled. This works to partially anonymise the input data (and therefore partially de-identifying the user data).
```

**Some examples of this (screenshots):**

*Handling 'sensor off' values for heart rate:* https://drive.google.com/file/d/1PrR4BwaIJNzcy2zXzqnGMzDz_wSopZbi/view?usp=share_link

*Transforming/rescaling input values for accelerometer (1):* https://drive.google.com/file/d/1TDEObkfyN0Whksq0VqtnpB8yAjEsc7rm/view?usp=share_link

*Transforming/rescaling input values for accelerometer (2):* https://drive.google.com/file/d/1k8XfHmxRaB_mXx2ipY5ZMAKYE67Ioqru/view?usp=share_link

## Designing for accessibility
Some information on this was included in the Pure Data patch in *./pd_patches/passive_inputs_test.pd* as comments on the main page:

```
Sensor inputs are used in various ways to create music. Active inputs (accelerometer, gyrometer) are responsible for a melodic instrument. Their values change the sound's volume/amplitude/dynamics and pitch/frequency respectively.
As human ears perceive changes in pitch and volume on logarithmic scales, a user expects a movement or position in a control scheme to correspond to an exponential mapping of values. To make the instrument more intuitive (especially so for those not familiar with musical instruments and music theory), active input values are re-scaled from exponential as linear mappings.
```
**Some examples of this (screenshots):**

*Transforming/rescaling input values for accelerometer:* https://drive.google.com/file/d/1k8XfHmxRaB_mXx2ipY5ZMAKYE67Ioqru/view?usp=share_link

*Transforming/rescaling input values for gyrometer (converting from an exponential MIDI note value to a linear frequency value):* https://drive.google.com/file/d/1NncmUweccGHtZTi9nM_TCK-KPwSPiLNV/view?usp=share_link


Rubatone currently bounds the pitch of its melody instrument to the 12 Tone Equal Temperament (12 TET) standard tuning. 12 Tone Equal Temperament is a pitch tuning system, which originated from European musical practices; in the modern day, this tuning system has seen widespread adoption across many cultures and societies, due to various influences (such as globalism, colonialism, and through various communication technologies such as the internet, causing the Westernisation and homogenisation of other cultures). Whilst this design choice may potentially limit users from creating sounds within other microtonal or xenharmonic tunings (which may limit the user's freedom of expression with the instrument, and also enforces a Western musical practice onto the user), it was decided that discretising the pitch would be the most appropriate option for accessibility. In order to create an instrument that was easy for first time users to pick up and play, we chose to limit the range of possible values and therefore degree of precision required over the pitch controls. 12 TET was chosen as the tuning system, as (for better or for worse), this tuning is recognisable and widely adopted by many cultures around the modern world. 12 tone tuning would therefore be considered subjectively 'pleasant' and 'in tune' by most potential users. The other musical components of Rubatone (the instrumental music generated by passive inputs) are also constrained to 12 TET tuning, to match the tonal content generated by active inputs.

There were concerns that fixing/discretising the pitches of the melody instrument to a greater extent (eg. fixing notes to rest within certain diatonic scales or chords) may be too restrictive, and may 'cheapen' the experience of playing the instrument by removing all elements of 'challenge' from using it. As such, we have tried to reach a middle-ground by creating an instrument that is not overly difficult to learn to play, but still has some small degree of practice required for a user to improve their skills. As well as this, not bounding pitch values to a scale or notes in a chord allows for users to have greater freedom over the harmonic content, being able to create interesting non-diatonic or polytonal/polymodal melodies if desired.

Accessibility for non-Western cultures was also considered during development. As we have chosen to bound pitch values to 12 TET, we have made a strong assumption that our target users will be familiar with the conventions of modern Western music. The instrument could potentially be made more accessible to other cultures by incorporating elements from non-Western musical practices. However, doing so has the strong potential for non-tasteful or exploitative appropriation of other cultural practices. None of the development team were well-versed in non-Western musical practices, and felt that they were not in a position to create music from such practices in an appropriate way. Rubatone's musical content attempts to draw from many modern musical influences. Some of these influences inevitably draw from non-Western traditions, which themselves influenced the development of modern Western music. For example, some music in the app utilises polyrhythms and complex time signatures, which were uncommon in traditional Western art music, but prominent in some traditional non-Western music practices. It is hoped that these particular influences are abstracted enough from their original cultural origins (being perceived as culturally agnostic in modern society due to a modern 'melting pot' of creative influences), and would not be viewed as distateful appropriation.


# Getting Started
## Required dependencies
- An Android SDK is required to run the project. Using Android SDK 34 is strongly recommended. Any SDK version 30 or later can be used with the project, but we have not tested
these to confirm if there are any issues.

- We have included *build.gradle.kts* and *settings.gradle.kts* files with the project. These include some specified dependencies/requirements for the Kortholt library to run. If using these same files, there should be no need to add/change anything within them for the project to run.

- If trying to adapt this code into a new project, here are some recommended changes to your gradle files:

    - Add Maven Central repository to the *settings.gradle.kts* file as a plugin and dependency:
    ```
    pluginManagement {
        repositories {
            google()
            mavenCentral()
            gradlePluginPortal()
        }
    }
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()
        }
    }
    ```

    - In the *build.gradle.kts* file in the *app* directory, the minimum SDK should be set
    to 28 or higher (a requirement for the Kortholt library):
    ```
    android {
        ...
        compileSdk = 34

        defaultConfig {
            applicationId = "com.example.Heartbeats_by_Dr_Dre"
            // For Kortholt library - the minimum SDK was changed from 24 to 28
            minSdk = 28
            ...
        }
        ...
    }
    ```

    - In the *build.gradle.kts* file in the *app* directory, the Java compile version and JVM target should be set to 11 (a requirement for the Kortholt library):
    ```
    android {
        ...
        // For Kortholt library - the Java compile version was changed from 1.8 to 11
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        // For Kortholt library - JVM target was changed from 1.8 to 11
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
        ...
    }
    ```

    - In the *build.gradle.kts* file in the *app* directory, Kortholt should be added as a dependency (a requirement for the Kortholt library):
    ```
    dependencies {
        ...
        implementation("net.simno.kortholt:kortholt:3.0.0")
    }
    ```


## How to build/run the project
- To run the code on phyiscal devices, you first need to load the project into 'Android Studio' (found at: https://developer.android.com/studio) onto your computer
- Note that this guide was created assuming you are using a phone with a modern version of Android and a watch using 'One UI' version 4.5. Instruction may differ if this is not what you are using

- After installing android studio, complete the following steps to load the project:
    - Open 'Android Studio'
    - In 'Android Studio', go to 'File', this should be at the top left of the window
    - Click 'Open'
    - Navigate to the project's folder
    - In the project's folder, select 'android-companion-app'
    - Click 'OK'
    - Once the project is loaded in, there should be a hammer icon near the top right of the screen, click on the dropdown box and select 'Make Project'

- To get a physical android phone to download the app, the following steps are required:
    - Enable 'Developer options', you can find how to do this for you phone via the internet
    - In 'Developer options', enable 'USB debugging'
    - Connect your phone via a USB cable to your computer
    - You should receive a request on your phone to 'Allow USB debugging?' access, click 'Allow'
    - Within 'Android Studio', there should be a dropdown box stating either 'app' or 'wearosapp' near the top right of the screen, click on the dropdown box and select 'app'
    - To the right of that dropdown box, there should be another dropdown box, click on that dropdown box and selects your device (It should be under "Running devices")
    - Within 'Android Studio', there should be a green arrow near the top right of the screen that says "Run 'app'" when you hover over it, click it
    - After it finishes loading (Note that this can take a while) the app should appear on your phone as 'Rubatone'

- To get a physical watch to download that app, the following steps are required:
    - Enable 'Developer options', you can find how to do this for you watch via the internet
    - Connect both your watch and your computer , make sure that this network allows for ADB debugging (most public/company/hotel networks do not for security reasons)
    - In 'Developer options', enable 'ADB debugging' and 'Debug over WI-FI'
    - In 'Developer options', select 'Wireless debugging' and enable 'Wireless debugging'
    - You should get a popup on your phone asking 'Allow debugging on this network?', select 'Allow'
    - Still in 'Wireless debugging', select 'Pair new device'
    - Under 'IP address and port', there should be an ip adress followed by a colon and then a port number
    - On your computer, navigate to where Android Studio has installed 'adb' and open it in a terminal/powershell
    - Within this window, type the command: ./adb connect ($S)
        -Where ($S) is the IP number your watch gave you
    - On your watch, you should now get a notification saying 'Allow Debugging?', select 'OK'
    - Within 'Android Studio', there should be a dropdown box stating either 'app' or 'wearosapp' near the top right of the screen, click on the dropdown box and select 'wearosapp'
    - To the right of that dropdown box, there should be another dropdown box, click on that dropdown box and selects your device (It should be under "Running devices")
    - Within 'Android Studio', there should be a green arrow near the top right of the screen that says "Run 'wearosapp'" when you hover over it, click it
    - After it finishes loading (Note that this can take a while) the app should appear on your watch as 'Rubatone'


# Important Project Files
- Mobile app code is located here: *./android-companion-app/app/src/main/java/com/example/Heartbeats_by_Dr_Dre/MainActivity.kt*

- Wearable app code is located here: *./android-companion-app/wearosapp/src/main/java/com/example/Heartbeats_by_Dr_Dre/MainActivity.kt*

- The music synthesis component referenced by the mobile app can be found in this location: *./android-companion-app/app/src/main/res/raw/passive_inputs_test.zip*.
To open this file as a standalone Pure Data patch, the same file can be found (uncompressed) here: *./pd_patches/passive_inputs_test.pd*

# Sources Used
- The 'Kortholt' library was used to integrate the music synthesis component with the mobile app: https://github.com/simonnorberg/kortholt

- 'Kortholt' utilises 'LibPD', a 'Pure Data' wrapper for other programming languages: https://github.com/libpd/libpd

- 'Pure Data' is a visual programming language used for real-time music generation: http://puredata.info/

- 'Kortholt' also uses Android's 'Oboe' library for handling Audio I/O: https://github.com/google/oboe

- 'Android SDK' and 'Jetpack Compose' were used to create the various mobile and wearable app components, including interfacing with hardware sensors and UI elements: https://developer.android.com/

# Music Synthesis Info
The music component is made with patches using Pure Data, a visual programming language.
These patches interface with the mobile app's project code by using Kortholt. Kortholt combines LibPD (a Pure Data wrapper that converts patches into a compilable format for the Kotlin code) and Android's Oboe library (used to handle the audio I/O, drivers, and various other settings to optimise for low latency).
The mobile app code will send float values for sensor inputs to the Pure Data patch, via Kortholt methods.
You can also use the Pure Data (PD) patches on their own, if you would like to test them, or see exactly how the music synthesis works in more detail. These patches can be found in the *./pd_patches* directory.

Here are some examples of what the various musical components sound like: https://drive.google.com/drive/folders/1vTTTFQ566BI69s2_AKwTh8KSf03fYj4L

If you want to see what the Pure Data patch used in Rubatone looks like/does, but don't want to download Pure Data, you can see brief video demonstrations here: https://drive.google.com/drive/folders/1-4VzrA4_uhGQU_6DVyq5SnZZO9ETCZWs

## Setting up Pure Data
To open these, download the most recent version of PD vanilla here:
https://puredata.info/downloads/pure-data

### Setting up audio:

1. Open up pd
2. In the menu bar, go to Media > Audio Settings...
3. Under Output Devices, select the audio output device that you're computer is currently using.
4. Click 'Apply', then click 'OK'

**To test that these settings work:**

1. Turn your volume down to a fairly low level.
2. In the top right hand corner, click on the 'DSP' button to tick it. Text next to the button should now say 'Audio on'
3. In the menu bar, go to Media > Test Audio and MIDI...
4. A pd patch will be opened. On the left hand side, under the 'TEST GAIN (dB)' heading, select the radio button with '60' next to it.
If you hear a sine tone, then the sound works. 
If not, the audio device is not configured correctly. Try changing the audio output device selected and/or the device sample rate settings in the Media > Audio Settings... menu.

For more info on audio setup, check here:
http://msp.ucsd.edu/Pd_documentation/x3.htm


#### A pre-emptive word of warning:
PD can be dangerous - when modifying things, you can accidentally make very loud sounds happen very suddenly and very easily.
Make sure that your device output volume is set to a fairly low level before you turn DSP (Digital Signal Processing) on and create any sound.


## Files in pd_patches directory:

### passive_inputs_test.pd

This patch contains a modified version of the MvP melodic synth (in the file *pulse_mockup_one_file.pd*), as well as additional music components which are generated by passive inputs. This patch is the version used in the final codebase by the mobile app. It contains accelerometer, gyrometer, and heart rate sensor inputs, as well as temperature and light sensor inputs. For more information on how this works, please see the comments added to the main page of the patch.

#### Receiver inputs used in the patch:
- **appOnOff:** toggles the audio on if a float value of 1.0 is sent. Toggles off if a float of 0.0 is sent.
- **appHeartRate:** receives a float value of the heart rate - changes the pulse tempo based on this value.
    Accepts all values, but bounds them to the range [50, 200] BPM. There are three tempo intensity states, in which different drum loops are chosen. Low intensity is in the ranges 50-79 BPM and 160-200 BPM, medium intensity is within 80-119 BPM, and high intensity is within 120-159 BPM.
- **appAccelerometer:** receives a float value of the intensity/force used for an arm movement, which is the magnitude of the linear acceleration across all 3 axes. Changes the volume/dynamics of the pulsing tone based on this value. More forceful arm movements will result in a louder sound; gentler arm movements result in a quieter sound.
    Currently configured to bound input values to the range 8 to 10. These values are then mapped from an exponential to a linear distribution, and rescaled to the range 0 to 0.3, to produce volume/amplitude values in this range. If there is no change in input values, or if values close to 7 are sent, the volume will gradually ramp down to 0 (within 500ms), as the implementation assumes that the user is no longer attempting to play a note.
- **appGyrometer:** receives a float value of the vertical position of the arm movement, which is the gyroscope's approximate angle of orientation for rotations about the x axis of the wearable (pitch rotations). Changes the pitch/frequency of the pulsing tone based on this value. The given orientation values are relative to (and based on) the previously calculated orientation value.
    Currently configured to bound input values to the range ~(-pi)/2 to ~pi/2, to give pitches in the range of two octaves (notes between C4 and C6). Tilting the device in the y axis upwards by ~90 degrees will result in the highest note C6 being played. Tilting the device downwards in this axis by ~90 degrees will play the lowest note C4. If the device face is held completely level in the x axis, the middle note of this range (C5) should be played. 
- **appPitchControl:** Changes the form of pitch control being used. Has this behaviour when sent the following float values:
    - 0.0: Fully continuous pitch (not fixed to a scale/tuning)
    - 1.0: Discrete pitch (fixed to 12 tone tuning)
    - 2.0: Discrete pitch, with a 100ms portamento between each note

    For Rubatone, the value sent to appPitchControl has been hardcoded to 2.0 (Discrete pitch with portamento).
- **appTemperature:** receives a float value of the ambient temperature (in degrees Celsius). Accepts all values, but bounds them to the range [0, 55] degrees Celsius. Values > 36.65 will be mapped to the high temperature state; values <= 36.65 will be mapped to the low temperature state.
- **appLight:** receives a float value of the light intensity (in lux). Accepts all values, but bounds them to the range [0, 5000] lux. Values > 25 will be mapped to the high light state; values <= 25 will be mapped to the low light state.

### pulse_mockup_one_file.pd
The patch pulse_mockup_one_file.pd contains a mockup synth controlled by a 'pulse' input analagous to a heart rate input (in beats per minute).
The pulsing sound can be turned on or off, and a variable tempo/heart rate value can be sent to change the pulsing frequency.
As well as this, a 'panic'/mute button is also included for disabling sound.
Both accelerometer and gyro inputs can be sent to the patch, to control loudness/dynamics and pitch of the synth tone respectively.
Info on how the patch works and what it does is included in comments inside of the patch.
This patch was originally used in the Minimum Viable Product (MvP) prototype. It was integrated into the file passive_inputs_test.pd when additional features and sensors were implemented.


## A bit more detail about how the music works (passive inputs)
Here are some examples of what the various musical components sound like: https://drive.google.com/drive/folders/1vTTTFQ566BI69s2_AKwTh8KSf03fYj4L

These components are partially generative or stochastic, meaning that they play sequenced loops for each of the instruments, with some elements chosen at random. For example, the bass and chords instruments will randomly choose to play one of three distinct sounding loops, which use different chords and notes. Certain drum hits and notes in the chord and bass section also have assigned probabilities that a sequenced note will play. This means that any session of playing/performing with Rubatone will be unique, and sound unlike the last!

The heart rate values are mapped to the music's tempo, with faster heart rates creating faster-paced music and slower heart rates creating slower music. As well as this, heart rate values are mapped to three discrete 'intensity' states (low, medium, and high intensity), with different drum loops and rhythms being chosen to match the intensity level.

Light intensity and temperature values are mapped to discretised state spaces (representing high or low value readings for each sensor), which combine together to create distinct musical sections. In total, there are 4 of these states - the music in each state will represent the state of the environment in a musical way:
- **High temperature and high light:** A calming section with saccharine, lush harmonies, in the key of C major. The chord synth is muted, intended to be bright, but calming in sound. The drums are minimal and on-beat, attempting to occasionally represent a 'heart beat' rhythm in 4/4 time signature. This section is trying to convey the mood/setting associated with seeing the sunrise on a warm Summer morning.
- **Low temperature and high light:** An ambient-leaning section primarily in the key of D major, with bright, but more 'frigid' sounding bell synths playing 5/4 and 4/4 polyrhythmic loops. This section contains miminal percussion, with an occasional low-end percussive element for additional texture. Musically, this is intended to convey a sunny, but brisk Autumn or Winter morning.
- **High temperature and low light:** A more 'aggressive' and 'driving' section in G minor, with flurried, distorted drum loops in 7/8 driving the music. A simple chord section creates minimal, swelling harmonies to complement. This section is intended to convey the feeling of a hot sub-tropical Summer night, representing the build-up of humidity directly preceding a storm.
- **Low temperature and low light:** Another ambient-leaning section in 3/4 with a muted and distant synth, playing chords in F major/D minor (with an uncertain tonal centre), creating a bittersweet and partly melancholic feel. A percussive element aims to create a digital 'wind-like' sound. The music in this state is aiming to invoke the feeling of a cold, windy Winter night, in which the user may long for a hot chocolate or coffee to keep them warm.

More generally speaking, high temperature states correspond to more rhythmically focused and densely layered sections, with an emphasis on instrumentation with a 'warmer' or more saturated/distorted tone. Lower temperature states are more ambient in nature, with less of a pronounced percussive element and synthesised tones that are more 'frigid'. High light states will gravitate more towards major or major-sounding tonalities, with lower light states gravitating towards either a minor key or a key with an uncertain tonal centre.

As well as this, temperature and light sensor values also map to continuous musical parameters, controlling audio effects. The temperature controls the volume of a bitcrusher effect on some of the instruments (increasing in volume when temperature increases), which makes some of the instruments sound more 'warmer'/saturated. Light sensor inputs change a lowpass filter applied to the audio - lower light levels result in the audio having less higher frequencies/higher pitches present, giving a muted and 'darker' sound.


