package com.byern.tanima;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class BallPainter {

  private int id;
  private Sprite ballSprite;
  private float x = 0;
  private float y = 250;
  private float size = 100;
  private float speed = 20f;

  private int maxW;

  private boolean rightDirection = true;
  private Optional<KryonetClientServer> maybeKryonet;
  private boolean master = true;
  private Lwjgl3Window window;

  public static class BallMessage {
    boolean rightDirection;
    float windowY;
    float y;
    int id;

    public BallMessage() {
    }

    public BallMessage(boolean rightDirection, float windowY, float y, int id) {
      this.rightDirection = rightDirection;
      this.windowY = windowY;
      this.y = y;
      this.id = id;
    }

    public boolean isRightDirection() {
      return rightDirection;
    }

    public void setRightDirection(boolean rightDirection) {
      this.rightDirection = rightDirection;
    }

    public float getWindowY() {
      return windowY;
    }

    public void setWindowY(float windowY) {
      this.windowY = windowY;
    }

    public float getY() {
      return y;
    }

    public void setY(float y) {
      this.y = y;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }
  }

  public BallPainter(int id, int w, int h, Optional<KryonetClientServer> maybeKryonet) {
    this.id = id;
    this.window = ((Lwjgl3Graphics) Gdx.graphics).getWindow();

    ballSprite = new Sprite(new Texture("badlogic.jpg"));
    this.maybeKryonet = maybeKryonet;
    maybeKryonet.ifPresent(k ->
       (k.config.isMaster() ? k.server : k.client).addListener(new Listener() {
      @Override
      public void received(Connection connection, Object object) {
        if (object instanceof String) {
          String msg = (String) object;
          String[] strings = msg.split(":");
          if (strings.length == 5 && strings[0].equals("Direction") && Integer.parseInt(strings[4]) == id) {
            rightDirection = Boolean.parseBoolean(strings[1]);
            float windowY = window.getPositionY();
            float receivedWindowY = Float.parseFloat(strings[2]);
            float receivedY = Float.parseFloat(strings[3]);
            y = receivedY + (windowY - receivedWindowY);
            /*
        if(object instanceof BallMessage) {
          BallMessage ballMessage = (BallMessage) object;
          if(ballMessage.id == id){
            rightDirection = ballMessage.rightDirection;
            float windowY = window.getPositionY();
            y = ballMessage.y + (windowY - ballMessage.windowY);*/

            master = true;
            if(rightDirection) {
              x = -size;
            } else {
              x = maxW;
            }
          }
        }
      }
    }));
    this.master = maybeKryonet.map(k -> k.config.isMaster()).orElse(true);
    maxW = w;
    Random random = new Random();
    x = random.nextInt((int) (maxW - size * 2));
    y = random.nextInt((int) (h - size));
  }

  public void paint(SpriteBatch spriteBatch) {
    if (master) {
      spriteBatch.draw(ballSprite, x, y, size, size);
      x += speed * (rightDirection ? 1 : -1);
      if (rightDirection && x > (maxW) || !rightDirection && x < -size) {
        if (maybeKryonet.isPresent() && maybeKryonet.get().canSend()) {
          maybeKryonet.get().send("Direction:" + rightDirection + ":" + window.getPositionY() + ":" + y + ":" + id);
          /*maybeKryonet.get().send(
             new BallPainter.BallMessage(
                rightDirection,
                window.getPositionY(),
                y,
                id
             )
          );*/
          master = false;
        }
        rightDirection = !rightDirection;
      }
    }
  }
}
