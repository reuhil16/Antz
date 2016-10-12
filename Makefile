JR = java
JC = javac

SOURCES = $(shell find src -iname *.java)
GDX_DIR = lib/gdx
OUT_DIR = class

all: compile pack_atlas

compile: $(SOURCES)
	@mkdir -p $(OUT_DIR)
	$(JC) -d $(OUT_DIR) -cp $(GDX_DIR)/gdx.jar:$(GDX_DIR)/gdx-backend-lwjgl.jar:$(GDX_DIR)/gdx-freetype.jar $^

pack_atlas:
	@mkdir -p $(OUT_DIR)
	$(JR) -cp $(GDX_DIR)/gdx.jar:$(GDX_DIR)/gdx-tools.jar com.badlogic.gdx.tools.texturepacker.TexturePacker assets/raw assets/ assets

clean:
	rm -rf $(OUT_DIR) assets/assets.atlas assets/assets*.png
