package gamewolves.itch.io.electrix.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;

import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

import box2dLight.PointLight;
import gamewolves.itch.io.electrix.Main;
import gamewolves.itch.io.electrix.physics.Filters;
import gamewolves.itch.io.electrix.physics.Physics;

public class Generator
{
    private Texture generatorTexture;
    private Animation<TextureRegion> generatorAnimation;
    private AnimatedSprite generator;

    private PointLight light;
    private Body body;
    private float hp;

    private Sound rumble;
    private long id;

    public Generator()
    {
        rumble = Gdx.audio.newSound(Gdx.files.internal("sounds/generator.wav"));
        id = rumble.loop();
        hp = 1;
        generatorTexture = new Texture(Gdx.files.internal("generator.png"));

        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < 14; i++)
            frames.add(new TextureRegion(generatorTexture, i * 128, 0, 128, 128));

        generatorAnimation = new Animation<>(0.05f, frames);
        generatorAnimation.setPlayMode(Animation.PlayMode.LOOP);
        generator = new AnimatedSprite(generatorAnimation);
        generator.setOriginCenter();
        generator.setOriginBasedPosition(0, 0);
        generator.play();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        body = Physics.getWorld().createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((generator.getWidth() / 2) * Main.MPP, (generator.getHeight() / 2) * Main.MPP);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.maskBits = Filters.MaskAny;
        fixtureDef.filter.groupIndex = 1;
        fixtureDef.filter.categoryBits = Filters.Generator;

        body.createFixture(fixtureDef);

        light = new PointLight(Physics.getRayHandler(), 500, new Color(0.25f, 0.25f, 0.25f, 0.5f), 10, 0, 0);
        light.setContactFilter(Filters.AnyNoMask, Filters.CategoryNone, Filters.MaskGenerator);
    }

    public void update(float dt, float volume)
    {
        hp += dt * 0.0025f;
        hp = Math.max(Math.min(hp, 1), 0);
        rumble.setVolume(id, volume);
    }

    public void render(SpriteBatch batch)
    {
        batch.begin();
        generator.draw(batch);
        batch.end();
    }

    public void damage(float dmg)
    {
        hp -= dmg;
    }

    public float getHP() {
        return hp;
    }

    public void dispose()
    {
        rumble.stop(id);
        rumble.dispose();
        generatorTexture.dispose();
    }
}
