/*
 * COSC326 - 2016 S2 - Étude 12 - Supersizing Ants
 * Thomas Farr, Reuben Hilder, Ben Scott
 * Java 8
 */

package gui;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import input.InputHandler;
import model.Universe;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class GraphicalFrontend implements ApplicationListener {
  private static final float ZOOM_PER_SEC        = 0.75f;
  private static final float TRANSLATION_PER_SEC = 640f;
  private static final float CELL_SIZE           = 64f;
  private static final float INITIAL_VIEW_SIZE   = 25 * CELL_SIZE;

  private final File     simulationSourceFile;
  private       Universe universe;
  private       boolean  running;
  private       float    unusedTime;
  private int stepsPerSec = 10;

  private Map<String, FreeTypeFontGenerator> fontGenerators = new HashMap<>();
  private OrthographicCamera camera;
  private SpriteBatch        batch;
  private ShapeRenderer      shapeRenderer;
  private TextureAtlas       textureAtlas;
  private Skin               skin;
  private Stage              uiStage;
  private Label              fpsLabel;
  private byte               zooming;
  private byte               horizontalTranslation;
  private byte               verticalTranslation;

  private GraphicalFrontend (File simulationSourceFile) {
    this.simulationSourceFile = simulationSourceFile;
    readUniverse();
  }

  public static void main (String[] args) throws FileNotFoundException {
    if (args.length < 1) {
      System.err.println(
          "usage: java gui.GraphicalFrontend <simulation_source_file>");
      System.exit(1);
    }
    File sourceFile = new File(args[0]);
    if (!sourceFile.exists() || !sourceFile.isFile()) {
      System.err.println("`" + args[0] + "` is not a file!");
      System.exit(1);
    }
    LwjglApplicationConfiguration.disableAudio = true;
    new LwjglApplication(new GraphicalFrontend(sourceFile), "Ants in ~SPACE~",
                         640, 640);
  }

  private void readUniverse () {
    try {
      this.universe = InputHandler
          .initialiseUniverse(new FileInputStream(simulationSourceFile));
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Simulation source file is unreadable!", e);
    }
  }

  /**
   * Called when the application is started, to load any resources and such.
   */
  @Override
  public void create () {
    float w = Gdx.graphics.getWidth();
    float h = Gdx.graphics.getHeight();

    // Initialise camera.
    camera =
        new OrthographicCamera(INITIAL_VIEW_SIZE, INITIAL_VIEW_SIZE * (h / w));
    camera.position.set(0, 0, 0);
    camera.update();

    batch = new SpriteBatch();
    shapeRenderer = new ShapeRenderer();

    // Load assets
    textureAtlas = new TextureAtlas(Gdx.files.internal("assets.atlas"));
    skin = new Skin(textureAtlas);
    skin.add("opensans_14_semibold", generateFont(14, "OpenSans-Semibold"));
    skin.add("opensans_16_regular", generateFont(16, "OpenSans-Regular"));
    skin.add("opensans_16_regular_stroke",
             generateFont(16, "OpenSans-Regular", 1, Color.BLACK));
    skin.load(Gdx.files.internal("ui.skin"));

    uiStage = new Stage(new ScreenViewport());

    InputMultiplexer inputMultiplexer = new InputMultiplexer();
    inputMultiplexer.addProcessor(uiStage);
    inputMultiplexer.addProcessor(new InputAdapter() {
      @Override
      public boolean keyDown (int keycode) {
        if (keycode == Input.Keys.PLUS || keycode == Input.Keys.MINUS) {
          zooming += keycode == Input.Keys.PLUS ? 1 : -1;
          return true;
        } else if (keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
          horizontalTranslation += keycode == Input.Keys.RIGHT ? 1 : -1;
          return true;
        } else if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN) {
          verticalTranslation += keycode == Input.Keys.UP ? 1 : -1;
          return true;
        }
        return false;
      }

      @Override
      public boolean keyUp (int keycode) {
        if (keycode == Input.Keys.PLUS || keycode == Input.Keys.MINUS) {
          zooming += keycode == Input.Keys.PLUS ? -1 : 1;
          return true;
        } else if (keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
          horizontalTranslation += keycode == Input.Keys.RIGHT ? -1 : 1;
          return true;
        } else if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN) {
          verticalTranslation += keycode == Input.Keys.UP ? -1 : 1;
          return true;
        }
        return false;
      }
    });
    Gdx.input.setInputProcessor(inputMultiplexer);

    Table table = new Table();
    table.setFillParent(true);
    table.add(new ButtonRow(skin)).pad(5).expand().fillX().align(Align.top);

    uiStage.addActor(table);

    fpsLabel = new Label("FPS: ##", skin, "withbg");
    fpsLabel.setPosition(10, 10);
    uiStage.addActor(fpsLabel);
  }

  private BitmapFont generateFont (int size, String fontName) {
    return generateFont(size, fontName, 0, Color.BLACK);
  }

  private BitmapFont generateFont (int size, String fontName, float borderWidth,
                                   Color borderColor) {
    FreeTypeFontGenerator generator = fontGenerators.get(fontName);

    if (generator == null) {
      generator = new FreeTypeFontGenerator(
          Gdx.files.internal("fonts/" + fontName + ".ttf"));
      fontGenerators.put(fontName, generator);
    }

    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
        new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = size;
    parameter.borderWidth = borderWidth;
    parameter.borderColor = borderColor;

    return generator.generateFont(parameter);
  }

  @Override
  public void resize (int width, int height) {
    camera.viewportWidth = INITIAL_VIEW_SIZE;
    camera.viewportHeight = INITIAL_VIEW_SIZE * height / width;
    camera.update();
    uiStage.getViewport().update(width, height, true);
  }

  /**
   * Called each time a frame should be rendered.
   */
  @Override
  public void render () {
    float delta = Gdx.graphics.getDeltaTime();

    // Do logic
    logic(delta);

    // Do drawing
    draw(delta);
  }

  /**
   * Perform all logic.
   *
   * @param delta The time elapsed since the last frame in seconds.
   */
  private void logic (float delta) {
    // Update FPS label
    fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());

    // Run Simulation
    if (running) {
      unusedTime += delta;
      int stepsToDo = MathUtils.floor(stepsPerSec * unusedTime);
      universe.moveNSteps(stepsToDo);
      unusedTime -= (float) stepsToDo / (float) stepsPerSec;
    }

    // Update UI stage
    uiStage.act(delta);

    // Camera zooming
    if (zooming != 0) {
      camera.zoom += -zooming * ZOOM_PER_SEC * delta;
      float minZoom = Gdx.graphics.getWidth() / camera.viewportWidth;
      if (camera.zoom < minZoom) {
        camera.zoom = minZoom;
      }
    }

    // Camera translation
    if (horizontalTranslation != 0 || verticalTranslation != 0) {
      camera.translate(horizontalTranslation * TRANSLATION_PER_SEC * delta,
                       verticalTranslation * TRANSLATION_PER_SEC * delta);
    }

    // Update camera
    camera.update();
  }

  /**
   * Perform the frame drawing.
   *
   * @param delta The time elapsed since the last frame in seconds.
   */
  private void draw (float delta) {
    // Update projection matrices
    batch.setProjectionMatrix(camera.combined);
    shapeRenderer.setProjectionMatrix(camera.combined);

    // Clear the screen
    Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // Draw cells
    float viewX = camera.position.x - camera.viewportWidth / 2f;
    float viewY = camera.position.y - camera.viewportHeight / 2f;
    int xCell = MathUtils.floor(viewX / CELL_SIZE);
    int yCell = MathUtils.floor(viewY / CELL_SIZE);
    int visWidthCells = MathUtils.ceil(camera.viewportWidth / CELL_SIZE) + 1;
    int visHeightCells = MathUtils.ceil(camera.viewportHeight / CELL_SIZE) + 1;

    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    char state;
    for (int y = yCell; y < yCell + visHeightCells; ++y) {
      for (int x = xCell; x < xCell + visWidthCells; ++x) {
        state = universe.getState(new Point(x, y));
        shapeRenderer.setColor(getColorForState(state));
        shapeRenderer
            .rect(x * CELL_SIZE + 4f, y * CELL_SIZE + 4f, CELL_SIZE - 8f,
                  CELL_SIZE - 8f);
      }
    }
    shapeRenderer.end();

    // Draw UI
    uiStage.draw();
  }

  private Color getColorForState (char state) {
    if (state == universe.defaultState) {
      return Color.WHITE;
    } else {
      int stateCount = universe.states.length;
      int stateIdx = -1;
      for (int i = 0; i < stateCount; ++i) {
        if (universe.states[i] == state) {
          stateIdx = i;
          break;
        }
      }
      if (stateIdx == -1) {
        return Color.WHITE;
      } else if (stateIdx == 1) {
        return Color.BLACK;
      }

      float sectorSize = 360f / (float) (stateCount - 2);
      float hue = sectorSize * (stateIdx - 2);

      return ColorUtils.HSV_to_RGB(hue, 100f, 100f);
    }
  }

  @Override
  public void pause () {
    // NO-OP
  }

  @Override
  public void resume () {
    // NO-OP
  }

  /**
   * Called when the application is closing, to dispose of any loaded resources.
   */
  @Override
  public void dispose () {
    uiStage.dispose();
    skin.dispose();
    fontGenerators.values().forEach(FreeTypeFontGenerator::dispose);
    textureAtlas.dispose();
    shapeRenderer.dispose();
    batch.dispose();
  }

  private class ButtonRow extends Table {
    private final TextButton  resetBtn;
    private final TextButton  playPauseBtn;
    private final NumberField stepsPerSecField;
    private final NumberField nStepsField;
    private final TextButton  doNStepsBtn;

    private ButtonRow (Skin skin) {
      super(skin);

      Table left = new Table(skin);

      resetBtn = new TextButton("Reset", skin);
      resetBtn.addListener(new ClickListener() {
        @Override
        public void clicked (InputEvent event, float x, float y) {
          readUniverse();
        }
      });
      left.add(resetBtn).width(200);

      add(left).expandY().align(Align.top);

      Table center = new Table(skin);

      playPauseBtn = new TextButton("Play", skin);
      playPauseBtn.addListener(new ClickListener(Input.Buttons.LEFT) {
        @Override
        public void clicked (InputEvent event, float x, float y) {
          if (!running) {
            stepsPerSec = stepsPerSecField.getValue();
          }
          running = !running;
          updateButtons();
        }
      });
      center.add(playPauseBtn).width(200);

      center.row();

      Table t = new Table(skin);

      Label l = new Label("Steps / sec:", skin, "white");
      l.setAlignment(Align.center);
      t.add(l).width(100);

      stepsPerSecField = new NumberField(10, skin);
      stepsPerSecField.setMin(1);
      t.add(stepsPerSecField).width(100);

      center.add(t).width(200);

      add(center).padLeft(5).padRight(5).expandX();

      Table right = new Table(skin);

      l = new Label("Do n Steps:", skin, "white");
      l.setAlignment(Align.center);
      right.add(l).width(200);

      right.row();

      t = new Table(skin);

      nStepsField = new NumberField(1, skin);
      nStepsField.setMin(1);

      t.add(nStepsField).width(100);

      doNStepsBtn = new TextButton("Go", skin);
      doNStepsBtn.addListener(new ClickListener(Input.Buttons.LEFT) {
        @Override
        public void clicked (InputEvent event, float x, float y) {
          if (!doNStepsBtn.isDisabled()) {
            universe.moveNSteps(nStepsField.getValue());
          }
        }
      });
      t.add(doNStepsBtn).width(100);

      right.add(t).width(200);

      add(right).expandY().align(Align.bottom);
    }

    private void updateButtons () {
      playPauseBtn.setText(running ? "Pause" : "Play");
      resetBtn.setDisabled(running);
      stepsPerSecField.setDisabled(running);
      nStepsField.setDisabled(running);
      doNStepsBtn.setDisabled(running);
    }
  }
}
