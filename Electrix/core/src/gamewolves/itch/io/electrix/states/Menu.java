package gamewolves.itch.io.electrix.states;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import gamewolves.itch.io.electrix.Main;
import gamewolves.itch.io.electrix.transitions.TransitionHandler;

public class Menu extends State
{
    private Texture background;
    private Rectangle playButton, quitButton;
    private float alpha;

    @Override
    public void init()
    {
        alpha = 1;
        background = new Texture(Gdx.files.internal("background_menu.png"));

        playButton = new Rectangle(904 , 232, 297, 109);
        quitButton = new Rectangle(904, 48, 297, 109);
    }

    @Override
    public void TouchEvent(Vector2 position)
    {
        if (playButton.contains(position))
        {
            TransitionHandler.setTransition(deltaTime -> {
                alpha -= deltaTime;
                alpha = Math.max(0, alpha);
                return alpha <= 0;
            }, () -> disposeable = true);
        }
        else if (quitButton.contains(position))
            Gdx.app.exit();
    }

    @Override
    public void DragEvent(Vector2 start, Vector2 current, Vector2 delta)
    {

    }

    @Override
    public void DragEventEnd(Vector2 end)
    {

    }

    @Override
    public void update(float deltaTime)
    {

    }

    @Override
    public void render(SpriteBatch batch)
    {
        batch.begin();
        batch.setColor(1, 1, 1, alpha);
        batch.draw(background, 0, 0);
        batch.setColor(1, 1, 1, 1);
        batch.end();
    }

    @Override
    public void renderUI(SpriteBatch batch)
    {

    }

    @Override
    public void dispose()
    {

    }
}
