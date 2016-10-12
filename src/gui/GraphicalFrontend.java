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
import model.Ant;
import model.Universe;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class GraphicalFrontend implements ApplicationListener {
  private static final float ZOOM_PER_SEC        = 0.75f;
  private static final float TRANSLATION_PER_SEC = 10f;
  private static final float CELL_SIZE           = 1f;
  private static final float INITIAL_VIEW_SIZE   = 25 * CELL_SIZE;
  private static final Color BG_COLOR            =
      new Color(0.4f, 0.4f, 0.4f, 1f);

  private final File     simulationSourceFile;
  private       Universe universe;
  private       boolean  running;
  private       float    unusedTime;
  private int stepsPerSec = 10;

  private Map<Character, Color>              stateColors    = new HashMap<>();
  private Map<String, Color>                 antTypeColors  = new HashMap<>();
  private Map<String, FreeTypeFontGenerator> fontGenerators = new HashMap<>();
  private OrthographicCamera camera;
  private SpriteBatch        batch;
  private ShapeRenderer      shapeRenderer;
  private TextureAtlas       textureAtlas;
  private Skin               skin;
  private Stage              uiStage;
  private BitmapFont         font;
  private Label              fpsLabel;
  private byte               zooming;
  private byte               horizontalTranslation;
  private byte               verticalTranslation;
  
  private boolean drawGridLines = true;

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
      universe = InputHandler
          .initialiseUniverse(new FileInputStream(simulationSourceFile));
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Simulation source file is unreadable!", e);
    }

    stateColors.clear();
    antTypeColors.clear();

    stateColors.put(universe.defaultState, Color.WHITE);

    if (universe.states.length > 1) {
      stateColors.put(universe.states[1], Color.BLACK);
      if (universe.states.length > 2) {
        float sector = 360f / (float) (universe.states.length - 2);

        for (int i = 2; i < universe.states.length; ++i) {
          stateColors.put(universe.states[i],
                          ColorUtils.HSV_to_RGB(sector * (i - 2), 100f, 100f));
        }
      }
    }

    String[] antTypes = universe.species.keySet().toArray(new String[0]);
    if (antTypes.length > 0) {
      /*antTypeColors.put(antTypes[0], Color.WHITE);

      if (antTypes.length > 1) {
        float sector = 360f / (float) (antTypes.length - 1);

        for (int i = 1; i < antTypes.length; ++i) {
          antTypeColors.put(antTypes[i], ColorUtils
              .HSV_to_RGB(sector * (i - 1), 100f, 100f));
        }
      }*/
      float sector = 360f / (float) antTypes.length;

      for (int i = 0; i < antTypes.length; ++i) {
        antTypeColors.put(antTypes[i], ColorUtils
            .HSV_to_RGB(sector * i, 100f, 100f));
      }
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
    font = generateFont(16, "OpenSans-Regular", 1, Color.BLACK);
    skin.add("opensans_16_regular_stroke", font);
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
        } else if (keycode == Input.Keys.NUM_0
                   || keycode == Input.Keys.NUMPAD_0) {
          camera.position.x = 0;
          camera.position.y = 0;
          camera.update();
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

      @Override
      public boolean touchDown (int screenX, int screenY, int pointer,
                                int button) {
        uiStage.setKeyboardFocus(null);
        return true;
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
      camera.zoom -= zooming * ZOOM_PER_SEC * delta;
      float viewW = camera.zoom * camera.viewportWidth;
      float magnification = Gdx.graphics.getWidth() / viewW;

      
      /*
      if (magnification < 10f || magnification > 100f) {
        
        magnification = MathUtils.clamp(magnification, 10f, 100f);
        camera.zoom =
            Gdx.graphics.getWidth() / magnification / camera.viewportWidth;
            
        drawGridLines = false;
      } else {
        drawGridLines = true;
      }
      */
      if (magnification < 10f) {
        
        drawGridLines = false;
      } else {
        drawGridLines = true;
      }
      
      if (magnification > 500f) {
        magnification = 500f;
        camera.zoom =
            Gdx.graphics.getWidth() / magnification / camera.viewportWidth;
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
    Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // Draw cells
    float viewW = camera.viewportWidth * camera.zoom;
    float viewH = camera.viewportHeight * camera.zoom;
    float viewX = camera.position.x - viewW / 2f;
    float viewY = camera.position.y - viewH / 2f;
    int xCell = MathUtils.floor(viewX / CELL_SIZE);
    int yCell = MathUtils.floor(viewY / CELL_SIZE);
    int visWidthCells = MathUtils.ceil(viewW / CELL_SIZE) + 1;
    int visHeightCells = MathUtils.ceil(viewH / CELL_SIZE) + 1;

    if (universe.wrap) {
      xCell = Math.max(xCell, -universe.width + 1);
      yCell = Math.max(yCell, -universe.height + 1);
      visWidthCells = Math.min(visWidthCells, universe.width - xCell);
      visHeightCells = Math.min(visHeightCells, universe.height - yCell);
    }

    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    char state;
    for (int y = yCell; y < yCell + visHeightCells; ++y) {
      for (int x = xCell; x < xCell + visWidthCells; ++x) {
        state = universe.getState(new Point(x, y));
        shapeRenderer.setColor(stateColors.get(state));
        if (drawGridLines) {
        shapeRenderer.rect((x + (1f / 16f) - 0.5f) * CELL_SIZE,
                           (y + (1f / 16f) - 0.5f) * CELL_SIZE,
                           (7f / 8f) * CELL_SIZE, (7f / 8f) * CELL_SIZE);
        } else {
          shapeRenderer.rect(x, y, CELL_SIZE, CELL_SIZE);
          
        }
      }
    }

    // Draw Ants
    float radius = (7f / 8f) * (7f / 8f) * CELL_SIZE / 2f;
    for (Ant ant : universe.population) {
      shapeRenderer.setColor(Color.BLACK);
      shapeRenderer
          .arc(ant.position.x * CELL_SIZE, ant.position.y * CELL_SIZE, radius,
               0f, 360f, 36);
      shapeRenderer.setColor(antTypeColors.get(ant.type.name));
      shapeRenderer.arc(ant.position.x * CELL_SIZE, ant.position.y * CELL_SIZE,
                        (7f / 8f) * radius, 0f, 360f, 36);
    }
    shapeRenderer.end();

    // Draw Legend
    drawLegend();

    // Draw UI
    uiStage.draw();
  }

  private void drawLegend () {
    batch.getProjectionMatrix()
         .setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    shapeRenderer.getProjectionMatrix()
                 .setToOrtho2D(0, 0, Gdx.graphics.getWidth(),
                               Gdx.graphics.getHeight());
    shapeRenderer.updateMatrices();

    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    float startY = Gdx.graphics.getHeight() - 120f;
    float y = startY - 16f;
    for (char s : universe.states) {
      shapeRenderer.setColor(Color.BLACK);
      shapeRenderer.rect(16f, y, 16f, 16f);
      shapeRenderer.setColor(stateColors.get(s));
      shapeRenderer.rect(17f, y + 1f, 14f, 14f);
      y -= 20f;
    }

    y -= 24f;

    for (String antType : antTypeColors.keySet()) {
      shapeRenderer.setColor(Color.BLACK);
      shapeRenderer.arc(24f, y + 8f, 8f, 0f, 360f, 36);
      shapeRenderer.setColor(antTypeColors.get(antType));
      shapeRenderer.arc(24f, y + 8f, 7f, 0f, 360f, 36);
      y -= 20f;
    }
    shapeRenderer.end();

    batch.begin();
    font.draw(batch, "States:", 16f, startY + 20f);
    y = startY - 2f;
    for (char s : universe.states) {
      font.draw(batch, "= " + s, 36f, y);
      y -= 20f;
    }

    y -= 4f;

    font.draw(batch, "Species:", 16f, y);
    y -= 20f;
    for (String antType : antTypeColors.keySet()) {
      font.draw(batch, "= " + antType, 36f, y);
      y -= 20f;
    }
    batch.end();
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
