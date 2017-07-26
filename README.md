# UnifyId
This is for UnifyId challenge.
I have created this project on Android studio using mac.

Comple, buildTool, minsdk and target sdk:

  compileSdkVersion 25
  buildToolsVersion "26.0.0"
  minSdkVersion 21
  targetSdkVersion 25    
Tested on one plus one android version 6.0.1

What i have done
1. I have used encryption for saving file on android securely
2. Used AES with SecretKeySpec, Cipher classes of android library.
3. Encrypted Images are stored in saved_images folder in root directory.

Challenges:
1. I have never operate camera programmatically from application, so learned that.
2. Used AES encryption on android device, usinf ciper and SecretKeySpec classes.

Future considerations:
1. Take image in backround at random time and kep updating them.
2. Use better algorithm for increption.
3. Some times image is very dark, its because shutter speed (Solvable issue)
4. Add notificatins so user can see pictures are taken.
