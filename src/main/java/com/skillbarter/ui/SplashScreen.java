package com.skillbarter.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Glitch-free cinematic splash screen.
 *
 * FIXES applied vs previous version:
 *   1. Uses JFrame (undecorated) instead of JWindow — more stable on all platforms
 *   2. Starts at opacity 0f BEFORE setVisible(true) — eliminates white flash
 *   3. No per-pixel alpha (removed setBackground transparent) — was causing flicker on Windows
 *   4. pack() + pre-render before show — first frame is ready before window appears
 *   5. EDT-safe opacity animation via Timer
 *
 * USAGE in MainApp.main():
 *     SplashScreen.show(() -> new MainApp().setVisible(true));
 */
public class SplashScreen extends JFrame {

    // ── Colours ────────────────────────────────────────────────
    static final Color BG_DEEP    = new Color(0x0E0602);
    static final Color BG_INNER   = new Color(0x1E0E04);
    static final Color GOLD       = new Color(0xC49A6C);
    static final Color GOLD_LIGHT = new Color(0xEDD9C0);
    static final Color GOLD_MUTED = new Color(0xA0714F);

    // ── Animation state ────────────────────────────────────────
    private float   drawAlpha   = 0f;   // controls content fade (0→1)
    private float   ringScale   = 0.3f;
    private float   emblemScale = 0.4f;
    private float   emblemAngle = -20f;
    private float   shimmerX    = 1.5f;
    private float   progress    = 0f;
    private float   spinAngle   = 0f;
    private boolean finishing   = false;

    private Timer   masterTimer;
    private Timer   autoTimer;
    private final   Runnable onFinish;

    // ── Particles ──────────────────────────────────────────────
    private static final int N = 60;
    private final float[] px=new float[N], py=new float[N];
    private final float[] pvx=new float[N],pvy=new float[N];
    private final float[] pa=new float[N], pdecay=new float[N], pr=new float[N];

    // ─────────────────────────────────────────────────────────────
    public SplashScreen(Runnable onFinish) {
        this.onFinish = onFinish;

        // ── FIX 1: undecorated JFrame instead of JWindow ──────
        setUndecorated(true);
        setSize(720, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // ── FIX 2: start fully transparent BEFORE setVisible ──
        try { setOpacity(0.0f); } catch (Exception ignored) {}

        // Init particles
        Random rng = new Random();
        for (int i = 0; i < N; i++) resetP(i, rng, true);

        SplashPanel panel = new SplashPanel();
        setContentPane(panel);

        // ── FIX 3: pack and pre-render before showing ──────────
        pack();
        setSize(720, 520);
        setLocationRelativeTo(null);
    }

    // ── Call this to display the splash ───────────────────────
    private void launch() {
        setVisible(true);

        // ── FIX 4: animate window opacity 0→1 first (50ms) ────
        // then start content animation
        Timer opacityIn = new Timer(16, null);
        float[] winOpacity = {0f};
        opacityIn.addActionListener(e -> {
            winOpacity[0] = Math.min(1f, winOpacity[0] + 0.06f);
            try { setOpacity(winOpacity[0]); } catch (Exception ignored) {}
            if (winOpacity[0] >= 1f) {
                opacityIn.stop();
                startContentAnimations(); // start drawing after window is fully shown
            }
        });
        opacityIn.start();
    }

    private void resetP(int i, Random rng, boolean anywhere) {
        px[i]     = rng.nextFloat() * 720;
        py[i]     = anywhere ? rng.nextFloat() * 520 : 525f;
        pvx[i]    = (rng.nextFloat() - .5f) * .4f;
        pvy[i]    = -rng.nextFloat() * .6f - .15f;
        pa[i]     = rng.nextFloat() * .5f + .1f;
        pdecay[i] = rng.nextFloat() * .003f + .001f;
        pr[i]     = rng.nextFloat() * 1.6f + .3f;
    }

    // ── Content animation loop ─────────────────────────────────
    private void startContentAnimations() {
        Random rng = new Random();
        masterTimer = new Timer(16, e -> {
            if (finishing) {
                drawAlpha -= .05f;
                repaint();
                if (drawAlpha <= 0f) {
                    masterTimer.stop();
                    // Fade out window opacity
                    Timer opOut = new Timer(16, null);
                    float[] wo = {1f};
                    opOut.addActionListener(ev -> {
                        wo[0] = Math.max(0f, wo[0] - .07f);
                        try { setOpacity(wo[0]); } catch (Exception ignored) {}
                        if (wo[0] <= 0f) {
                            opOut.stop();
                            setVisible(false);
                            dispose();
                            SwingUtilities.invokeLater(onFinish);
                        }
                    });
                    opOut.start();
                }
                return;
            }
            // Animate content
            if (drawAlpha   < 1f) drawAlpha   = Math.min(1f, drawAlpha   + .018f);
            if (ringScale   < 1f) ringScale   = Math.min(1f, ringScale   + .014f);
            if (emblemScale < 1f) emblemScale = Math.min(1f, emblemScale + .018f);
            if (emblemAngle < 0f) emblemAngle = Math.min(0f, emblemAngle + .5f);
            if (drawAlpha   > .6f) shimmerX  -= .007f;
            if (shimmerX    < -1.5f) shimmerX = 1.5f;
            if (progress    < 1f)   progress += .0033f;
            spinAngle = (spinAngle + .4f) % 360f;
            for (int i = 0; i < N; i++) {
                px[i] += pvx[i]; py[i] += pvy[i]; pa[i] -= pdecay[i];
                if (pa[i] <= 0 || py[i] < -6) resetP(i, rng, false);
            }
            repaint();
        });
        masterTimer.start();

        autoTimer = new Timer(5000, e -> finish());
        autoTimer.setRepeats(false);
        autoTimer.start();
    }

    void finish() {
        if (finishing) return;
        finishing = true;
        if (autoTimer != null) autoTimer.stop();
    }

    // ── Drawing ────────────────────────────────────────────────
    private class SplashPanel extends JPanel {
        SplashPanel() {
            setPreferredSize(new Dimension(720, 520));
            // ── FIX 5: opaque panel — no transparency issues ───
            setOpaque(true);
            setBackground(BG_DEEP);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { finish(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            int W = getWidth(), H = getHeight(), cx = W/2, cy = H/2;
            float a = Math.min(1f, Math.max(0f, drawAlpha));

            // Solid background (panel bg handles base, we just ensure it)
            g2.setColor(BG_DEEP);
            g2.fillRect(0, 0, W, H);

            // Ambient glow
            glow(g2, -60,-80,320, new Color(0x8B5E3C), .15f*a);
            glow(g2, W-200,H-160,260, GOLD, .08f*a);
            glow(g2, cx-90,cy-90,180, GOLD, .07f*a);

            // Particles
            for (int i = 0; i < N; i++) {
                float al = pa[i]*a; if (al<=0) continue;
                g2.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,al));
                g2.fill(new Ellipse2D.Float(px[i]-pr[i],py[i]-pr[i],pr[i]*2,pr[i]*2));
            }

            // Rings
            float[] radii = {230f,172f,118f,74f};
            for (int i = 0; i < radii.length; i++) {
                float r = radii[i]*ringScale;
                float pulse = (float)(Math.sin(System.currentTimeMillis()/1000.0*Math.PI*.7+i*.8)*.5+.5);
                float ra = (.07f+i*.02f+pulse*.10f)*a;
                g2.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,ra));
                g2.setStroke(new BasicStroke(i==3?.9f:.5f));
                g2.drawOval((int)(cx-r),(int)(cy-r),(int)(2*r),(int)(2*r));
            }

            // Emblem
            Graphics2D eg = (Graphics2D)g2.create();
            eg.translate(cx, cy-150);
            eg.rotate(Math.toRadians(emblemAngle));
            eg.scale(emblemScale, emblemScale);
            int er = 52;
            Graphics2D sg = (Graphics2D)eg.create();
            sg.rotate(Math.toRadians(spinAngle));
            sg.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,.4f*a));
            sg.setStroke(new BasicStroke(.9f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1f,new float[]{4f,7f},0f));
            sg.drawOval(-(er+11),-(er+11),(er+11)*2,(er+11)*2);
            sg.dispose();
            eg.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,.4f*a));
            eg.setStroke(new BasicStroke(1f));
            eg.drawOval(-er,-er,er*2,er*2);
            eg.setColor(BG_INNER);
            eg.fillOval(-er+2,-er+2,(er-2)*2,(er-2)*2);
            eg.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,.5f*a));
            eg.setStroke(new BasicStroke(.6f));
            eg.drawOval(-er+2,-er+2,(er-2)*2,(er-2)*2);
            eg.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,a));
            eg.setFont(new Font("Georgia",Font.BOLD,26));
            FontMetrics fm=eg.getFontMetrics();
            String mono="SB"; eg.drawString(mono,-fm.stringWidth(mono)/2,fm.getAscent()/2-2);
            eg.setFont(new Font("SansSerif",Font.PLAIN,7)); fm=eg.getFontMetrics();
            eg.setColor(new Color(GOLD_MUTED.getRed()/255f,GOLD_MUTED.getGreen()/255f,GOLD_MUTED.getBlue()/255f,a));
            String t1="SKILL"; eg.drawString(t1,-fm.stringWidth(t1)/2,-er+20);
            String t2="BARTERS"; eg.drawString(t2,-fm.stringWidth(t2)/2,er-10);
            eg.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,.5f*a));
            diamond(eg,0,-(er+13),4); diamond(eg,0,er+13,4);
            diamond(eg,-(er+13),0,4); diamond(eg,er+13,0,4);
            eg.dispose();

            // Brand name
            int nameY = cy+32;
            g2.setFont(new Font("Georgia",Font.BOLD,50));
            fm=g2.getFontMetrics();
            String p1="Skill ",p2="Barters";
            int totalW=fm.stringWidth(p1+p2), nameX=cx-totalW/2;
            GradientPaint shim=new GradientPaint(cx+shimmerX*totalW,nameY-50,new Color(1f,1f,1f,0f),cx+shimmerX*totalW+80,nameY,new Color(1f,.9f,.7f,.5f*a));
            g2.setPaint(shim); g2.drawString(p1+p2,nameX,nameY);
            g2.setColor(new Color(GOLD_LIGHT.getRed()/255f,GOLD_LIGHT.getGreen()/255f,GOLD_LIGHT.getBlue()/255f,a));
            g2.drawString(p1,nameX,nameY);
            g2.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,a));
            g2.drawString(p2,nameX+fm.stringWidth(p1),nameY);

            // Caption
            int capY=nameY+26;
            g2.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,.4f*a));
            g2.setStroke(new BasicStroke(.8f));
            g2.drawLine(cx-160,capY-4,cx-80,capY-4);
            g2.drawLine(cx+80,capY-4,cx+160,capY-4);
            g2.setColor(new Color(GOLD_MUTED.getRed()/255f,GOLD_MUTED.getGreen()/255f,GOLD_MUTED.getBlue()/255f,a));
            g2.setFont(new Font("SansSerif",Font.ITALIC,11)); fm=g2.getFontMetrics();
            String cap="Skill In  \u2022  Value Out";
            g2.drawString(cap,cx-fm.stringWidth(cap)/2,capY);

            // Gold rule
            int rY=capY+22;
            g2.setPaint(new GradientPaint(cx-100,rY,new Color(0,0,0,0),cx,rY,new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),(int)(110*a))));
            g2.setStroke(new BasicStroke(.8f));
            g2.drawLine(cx-100,rY,cx,rY);
            g2.setPaint(new GradientPaint(cx,rY,new Color(GOLD.getRed(),GOLD.getGreen(),GOLD.getBlue(),(int)(110*a)),cx+100,rY,new Color(0,0,0,0)));
            g2.drawLine(cx,rY,cx+100,rY);

            // Enter button
            int bw=150,bh=38,bx=cx-bw/2,by=rY+18;
            g2.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,a));
            g2.fillRoundRect(bx,by,bw,bh,4,4);
            int bk=10;
            g2.setColor(new Color(GOLD_LIGHT.getRed()/255f,GOLD_LIGHT.getGreen()/255f,GOLD_LIGHT.getBlue()/255f,.4f*a));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(bx-7,by-7,bx-7+bk,by-7);     g2.drawLine(bx-7,by-7,bx-7,by-7+bk);
            g2.drawLine(bx+bw+7-bk,by-7,bx+bw+7,by-7); g2.drawLine(bx+bw+7,by-7,bx+bw+7,by-7+bk);
            g2.drawLine(bx-7,by+bh+7-bk,bx-7,by+bh+7); g2.drawLine(bx-7,by+bh+7,bx-7+bk,by+bh+7);
            g2.drawLine(bx+bw+7-bk,by+bh+7,bx+bw+7,by+bh+7); g2.drawLine(bx+bw+7,by+bh+7-bk,bx+bw+7,by+bh+7);
            g2.setColor(new Color(BG_DEEP.getRed()/255f,BG_DEEP.getGreen()/255f,BG_DEEP.getBlue()/255f,a));
            g2.setFont(new Font("SansSerif",Font.BOLD,11)); fm=g2.getFontMetrics();
            String en="E N T E R";
            g2.drawString(en,cx-fm.stringWidth(en)/2,by+bh/2+fm.getAscent()/2-2);

            // Progress bar
            int pbW=140,pbH=2,pbX=cx-pbW/2,pbY=by+bh+18;
            g2.setColor(new Color(GOLD.getRed()/255f,GOLD.getGreen()/255f,GOLD.getBlue()/255f,.15f*a));
            g2.fillRoundRect(pbX,pbY,pbW,pbH,2,2);
            g2.setPaint(new GradientPaint(pbX,pbY,GOLD_MUTED,pbX+(int)(pbW*progress),pbY,GOLD_LIGHT));
            g2.fillRoundRect(pbX,pbY,(int)(pbW*progress),pbH,2,2);

            // Hint
            g2.setColor(new Color(GOLD_MUTED.getRed()/255f,GOLD_MUTED.getGreen()/255f,GOLD_MUTED.getBlue()/255f,.35f*a));
            g2.setFont(new Font("SansSerif",Font.ITALIC,10)); fm=g2.getFontMetrics();
            String hint="click anywhere or wait to continue";
            g2.drawString(hint,cx-fm.stringWidth(hint)/2,H-14);

            g2.dispose();
        }

        void glow(Graphics2D g2,int x,int y,int sz,Color c,float alpha) {
            RadialGradientPaint rg=new RadialGradientPaint(new Point2D.Float(x+sz/2f,y+sz/2f),sz/2f,
                new float[]{0f,1f},new Color[]{
                    new Color(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,alpha),
                    new Color(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,0f)});
            g2.setPaint(rg); g2.fillOval(x,y,sz,sz);
        }

        void diamond(Graphics2D g2,int x,int y,int r) {
            g2.fillPolygon(new int[]{x,x+r,x,x-r},new int[]{y-r,y,y+r,y},4);
        }
    }

    // ── Public launcher ────────────────────────────────────────
    public static void show(Runnable afterSplash) {
        SwingUtilities.invokeLater(() -> {
            SplashScreen s = new SplashScreen(afterSplash);
            s.launch();  // launch() instead of setVisible() directly
        });
    }
}