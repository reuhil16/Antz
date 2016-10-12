#!/usr/bin/env bash

JAVA=java
GDX_DIR=lib/gdx
CLASSPATH="class:assets"
CLASSPATH+=":$GDX_DIR/gdx.jar"
CLASSPATH+=":$GDX_DIR/gdx-natives.jar"
CLASSPATH+=":$GDX_DIR/gdx-backend-lwjgl.jar"
CLASSPATH+=":$GDX_DIR/gdx-backend-lwjgl-natives.jar"
CLASSPATH+=":$GDX_DIR/gdx-freetype.jar"
CLASSPATH+=":$GDX_DIR/gdx-freetype-natives.jar"

$JAVA -cp $CLASSPATH gui.GraphicalFrontend
