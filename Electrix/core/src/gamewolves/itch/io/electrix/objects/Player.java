package gamewolves.itch.io.electrix.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;

import net.dermetfan.gdx.graphics.g2d.AnimatedSprite;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import gamewolves.itch.io.electrix.Main;
import gamewolves.itch.io.electrix.input.InputHandler;
import gamewolves.itch.io.electrix.physics.Filters;
import gamewolves.itch.io.electrix.physics.Physics;
import gamewolves.itch.io.electrix.states.Game;

public class Player
{
    private static final float MaxSpeed = 2.5f;
    private static final float MinSpeed = 1.25f;
    private static final float BaseEnergyLoss = .015f;
    private static final float BaseEnergyGain = .1f;
    private static final float MaxAngle = 45f;

    private AnimatedSprite sprite;
    private Texture playerIdleTexture;
    private Animation<TextureRegion> playerIdle;
    private ConeLight light;
    private ConeLight light2;
    private Body body;

    private boolean pressed;

    private float energy;

    public Player()
    {
        Array<TextureRegion> frames = new Array<>();
        playerIdleTexture = new Texture(Gdx.files.internal("player.png"));
        for (int i = 0; i < 8; i++)
            frames.add(new TextureRegion(playerIdleTexture, i * 32, 0, 32, 32));

        playerIdle = new Animation<>(0.25f, frames);
        playerIdle.setPlayMode(Animation.PlayMode.LOOP);
        sprite = new AnimatedSprite(playerIdle);
        sprite.setOriginCenter();
        sprite.setPosition(Main.Camera.viewportWidth / 2, Main.Camera.viewportHeight / 2);
        sprite.play();

        light = new ConeLight(Physics.getRayHandler(), 500, new Color(0.6f, 0.6f, 0.4f, 0.5f), 10, sprite.getX() + sprite.getWidth() / 2, sprite.getY() + sprite.getHeight() / 2, sprite.getRotation(), MaxAngle);
        light.setSoft(true);
        light.setSoftnessLength(0.6f);
        light.setContactFilter(Filters.AnyNoMask, Filters.CategoryNone, Filters.MaskLight);

        light2 = new ConeLight(Physics.getRayHandler(), 500, new Color(0.6f, 0.6f, 0.4f, 0.5f), 0.7f, sprite.getX() + sprite.getWidth() / 2, sprite.getY() + sprite.getHeight() / 2, sprite.getRotation() - 90, 180 - MaxAngle);
        light2.setSoft(true);
        light2.setSoftnessLength(0.6f);
        light2.setContactFilter(Filters.AnyNoMask, Filters.CategoryNone, Filters.MaskLight);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;

        body = Physics.getWorld().createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((sprite.getWidth() / 2) * Main.MPP, (sprite.getHeight() / 2) * Main.MPP);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.filter.categoryBits = Filters.Player;
        fixtureDef.shape = shape;
        fixtureDef.friction = 3f;

        body.createFixture(fixtureDef);

        body.setTransform(0, 1, 0);

        energy = 1;
        pressed = false;
    }

    public void handleInput(float dt, boolean isControllerConnected, Vector2 controllerSpeed)
    {
        if (!isControllerConnected)
            sprite.setRotation(InputHandler.CurrentMousePos.sub(new Vector2(sprite.getX() + sprite.getOriginX(), sprite.getY() + sprite.getOriginY())).angle());

        Vector2 speed = new Vector2();

        if (isControllerConnected)
        {
            speed = controllerSpeed.cpy();

            Vector2 value = new Vector2(Controllers.getControllers().first().getAxis(1), -Controllers.getControllers().first().getAxis(0));

            if (value.len() > 0.05f)
                sprite.setRotation(value.angle());

            if (Controllers.getControllers().first().getButton(5))
            {
                if (!pressed && energy > 0) {
                    energy -= 0.035;
                    Game.Instance.shots.add(new Shot(body.getPosition().scl(1 / Main.MPP), new Vector2(MathUtils.cosDeg(sprite.getRotation()), MathUtils.sinDeg(sprite.getRotation()))));
                }
                pressed = true;
            }
            else
                pressed = false;

        }
        else
        {
            if (Gdx.input.isKeyPressed(Input.Keys.W))
                speed.y = 1;
            if (Gdx.input.isKeyPressed(Input.Keys.A))
                speed.x = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.S))
                speed.y = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.D))
                speed.x = 1;

            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            {
                if (!pressed && energy > 0) {
                    energy -= 0.035;
                    Game.Instance.shots.add(new Shot(body.getPosition().scl(1 / Main.MPP), new Vector2(MathUtils.cosDeg(sprite.getRotation()), MathUtils.sinDeg(sprite.getRotation()))));
                }

                pressed = true;
            }
            else
                pressed = false;
        }

        speed.nor();

        if (energy <= 0)
            speed.scl(MinSpeed);
        else
            speed.scl(MaxSpeed);


            Vector2 dV = speed.cpy().sub(body.getLinearVelocity());

            Vector2 f = dV.scl(body.getMass()).scl(1 / dt);

            body.applyForceToCenter(f, true);
    }

    public void update(float dt, boolean chargeable)
    {
        sprite.setPosition(body.getPosition().x * (1 / Main.MPP) - sprite.getWidth() / 2, body.getPosition().y * (1 / Main.MPP) - sprite.getHeight() / 2);
        sprite.update(dt);
        light.setConeDegree(Math.max(MaxAngle * energy, 10));
        light.setColor(new Color(0.6f, 0.6f, 0.4f, Math.max(0.5f * energy, 0.1f)));
        light.setDirection(sprite.getRotation());
        light2.setDirection(sprite.getRotation() - 180);
        light2.setConeDegree(180 - Math.max(MaxAngle * energy, 10));
        light.setPosition(body.getPosition());
        light2.setPosition(body.getPosition());

        Main.Camera.position.set(sprite.getX() + sprite.getOriginX(), sprite.getY() + sprite.getOriginY(), 0);

        if (body.getPosition().len() < 3 && chargeable)
            energy += BaseEnergyGain * dt;

        energy -= BaseEnergyLoss * dt;
        energy = Math.min(Math.max(energy, 0), 1);
    }

    public void render(SpriteBatch batch)
    {
        batch.begin();
        sprite.draw(batch);
        batch.end();
    }

    public float getEnergy()
    {
        return energy;
    }

    public void addEnergy(float energy)
    {
        this.energy += energy;
    }

    public AnimatedSprite getSprite() {
        return sprite;
    }

    public Body getBody() {
        return body;
    }
}
