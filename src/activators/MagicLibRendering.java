package activators;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class MagicLibRendering {
    public static final float UIscaling = Global.getSettings().getScreenScaleMult();
    private static final Vector2f PERCENTBARVEC1 = new Vector2f(21f, 0f);
    private static final Vector2f PERCENTBARVEC2 = new Vector2f(50f, 58f);

    public static LazyFont.DrawableString TODRAW14;
    public static LazyFont.DrawableString TODRAW10;

    //Color of the HUD when the ship is alive or the hud
    public static final Color GREENCOLOR;
    //Color of the HUD when the ship is not alive.
    public static final Color BLUCOLOR;
    //Color of the HUD for the red color.
    public static final Color REDCOLOR;

    static {
        GREENCOLOR = Global.getSettings().getColor("textFriendColor");
        BLUCOLOR = Global.getSettings().getColor("textNeutralColor");
        REDCOLOR = Global.getSettings().getColor("textEnemyColor");

        try {
            LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
            TODRAW14 = fontdraw.createText();
        } catch (FontException ex) {
        }
    }

    public static float getTextWidth(String text) {
        String oldText = TODRAW14.getText();
        TODRAW14.setText(text);
        float width = TODRAW14.getWidth();
        TODRAW14.setText(oldText);
        return width;
    }

    public static void setTextAligned(LazyFont.TextAlignment alignment) {
        TODRAW14.setAlignment(alignment);
    }

    public static Vector2f getHUDRightOffset(ShipAPI ship) {
        WeaponGroupAPI selectedGroup = ship.getSelectedGroupAPI();

        float weaponBarHeight = 0f;
        if (selectedGroup != null && selectedGroup.getWeaponsCopy() != null)
            weaponBarHeight = selectedGroup.getWeaponsCopy().size() * 12f;

        Vector2f baseBarLoc = new Vector2f(529f, 40f + weaponBarHeight);
        Vector2f shipOffset = getUIElementOffset(ship, ship.getVariant(), PERCENTBARVEC1, PERCENTBARVEC2);
        Vector2f adjustedLoc = Vector2f.add(baseBarLoc, shipOffset, null);
        return adjustedLoc;
    }

    /**
     * Draws a small UI bar above the flux bar. The HUD color change to blu when
     * the ship is not alive. //TODO: Bug: When you left the battle, the hud
     * keep for qew second, no solution found. //TODO: Bug: Also for other
     * normal drawBox, when paused, they switch brutally of "color".
     *
     * @param ship        Ship concerned (the element will only be drawn if that ship
     *                    is the player ship)
     * @param fill        Filling level
     * @param innerColor  Color of the bar. If null, use the vanilla HUD color.
     * @param borderColor Color of the border. If null, use the vanilla HUD
     *                    color.
     * @param secondfill  Like the hardflux of the fluxbar. 0 per default.
     * @param screenPos   The position on the Screen.
     */
    public static void addBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill, Vector2f screenPos) {
        addBar(ship, fill, innerColor, borderColor, secondfill, screenPos, 6 * UIscaling, 59 * UIscaling, true);
    }

    /**
     * Draws a small UI bar above the flux bar. The HUD color change to blu when
     * the ship is not alive. //TODO: Bug: When you left the battle, the hud
     * keep for qew second, no solution found. //TODO: Bug: Also for other
     * normal drawBox, when paused, they switch brutally of "color".
     *
     * @param ship        Ship concerned (the element will only be drawn if that ship
     *                    is the player ship)
     * @param fill        Filling level
     * @param innerColor  Color of the bar. If null, use the vanilla HUD color.
     * @param borderColor Color of the border. If null, use the vanilla HUD
     *                    color.
     * @param secondfill  Like the hardflux of the fluxbar. 0 per default.
     * @param screenPos   The position on the Screen.
     * @param boxHeight   Height of the drawn box.
     * @param boxWidth    Width of the drawn box.
     * @param drawBorders
     */
    public static void addBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill, Vector2f screenPos, float boxHeight, float boxWidth, boolean drawBorders) {
        final Vector2f boxLoc = new Vector2f(screenPos);
        final Vector2f shadowLoc = new Vector2f(boxLoc.getX() + 1f, boxLoc.getY() - 1f);
        boxLoc.scale(UIscaling);
        shadowLoc.scale(UIscaling);

        // Used to properly interpolate between colors
        float alpha = 1f;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            return;
        }

        Color innerCol = innerColor == null ? GREENCOLOR : innerColor;
        Color borderCol = borderColor == null ? GREENCOLOR : borderColor;
        if (!ship.isAlive()) {
            innerCol = BLUCOLOR;
            borderCol = BLUCOLOR;
        }
        int pixelHardfill = 0;
        float hardfill = secondfill < 0 ? 0 : secondfill;
        hardfill = hardfill > 1 ? 1 : hardfill;
        if (hardfill >= fill) {
            hardfill = fill;
        }
        pixelHardfill = (int) (boxWidth * hardfill);
        pixelHardfill = pixelHardfill <= 3 ? -pixelHardfill : -3;

        int hfboxWidth = (int) (boxWidth * hardfill);
        int fboxWidth = (int) (boxWidth * fill);

        OpenGLBar(ship, alpha, borderCol, innerCol, fboxWidth, hfboxWidth, boxHeight, boxWidth, pixelHardfill, shadowLoc, boxLoc, drawBorders);
    }

    /**
     * Draw text with the font victor10 where you want on the screen.
     *
     * @param ship      The player ship.
     * @param text      The text.
     * @param textColor The color of the text
     * @param screenPos The position on the Screen.
     */
    public static void addText(ShipAPI ship, String text, Color textColor, Vector2f screenPos) {
        Color borderCol = textColor == null ? GREENCOLOR : textColor;
        if (!ship.isAlive()) {
            borderCol = BLUCOLOR;
        }
        float alpha = 1f;
        if (Global.getCombatEngine().getCombatUI() == null || Global.getCombatEngine().isUIShowingDialog()) {
            return;
        }
        Color shadowcolor = new Color(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        Color color = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (borderCol.getAlpha() / 255f)
                        * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));

        final Vector2f boxLoc = new Vector2f(screenPos);
        final Vector2f shadowLoc = new Vector2f(screenPos.getX() + 1f, screenPos.getY() - 1f);

        TODRAW14.setText(text);
        TODRAW14.setColor(shadowcolor);
        TODRAW14.draw(shadowLoc);
        TODRAW14.setColor(color);
        TODRAW14.draw(boxLoc);
    }

    public static void OpenGLBar(ShipAPI ship, float alpha, Color borderCol, Color innerCol, int fboxWidth, int hfboxWidth, float boxHeight, float boxWidth, int pixelHardfill, Vector2f shadowLoc, Vector2f boxLoc, boolean drawBorders) {
        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());

        // Set OpenGL flags
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, 0, height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

        if (ship.isAlive()) {
            // Render the drop shadow
            if (fboxWidth != 0) {
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
                GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                        1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
                GL11.glVertex2f(shadowLoc.x - 1, shadowLoc.y);
                GL11.glVertex2f(shadowLoc.x + fboxWidth, shadowLoc.y);
                GL11.glVertex2f(shadowLoc.x - 1, shadowLoc.y + boxHeight + 1);
                GL11.glVertex2f(shadowLoc.x + fboxWidth, shadowLoc.y + boxHeight + 1);
                GL11.glEnd();
            }
        }

        if (drawBorders) {
            // Render the drop shadow of border.
            GL11.glBegin(GL11.GL_LINES);
            GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                    1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
            GL11.glVertex2f(shadowLoc.x + hfboxWidth + pixelHardfill, shadowLoc.y - 1);
            GL11.glVertex2f(shadowLoc.x + 3 + hfboxWidth + pixelHardfill, shadowLoc.y - 1);
            GL11.glVertex2f(shadowLoc.x + hfboxWidth + pixelHardfill, shadowLoc.y + boxHeight);
            GL11.glVertex2f(shadowLoc.x + 3 + hfboxWidth + pixelHardfill, shadowLoc.y + boxHeight);
            GL11.glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y);
            GL11.glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y + boxHeight);

            // Render the border transparency fix
            GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                    1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
            GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y - 1);
            GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y - 1);
            GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
            GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
            GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
            GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);

            // Render the border
            GL11.glColor4f(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                    alpha * (1 - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
            GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y - 1);
            GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y - 1);
            GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
            GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
            GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
            GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);
            GL11.glEnd();
        }

        // Render the fill element
        if (ship.isAlive()) {
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glColor4f(innerCol.getRed() / 255f, innerCol.getGreen() / 255f, innerCol.getBlue() / 255f,
                    alpha * (innerCol.getAlpha() / 255f)
                            * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
            GL11.glVertex2f(boxLoc.x, boxLoc.y);
            GL11.glVertex2f(boxLoc.x + fboxWidth, boxLoc.y);
            GL11.glVertex2f(boxLoc.x, boxLoc.y + boxHeight);
            GL11.glVertex2f(boxLoc.x + fboxWidth, boxLoc.y + boxHeight);
            GL11.glEnd();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();

    }

    /**
     * Get the UI Element Offset for the player on the center. (Depends of the
     * collision radius and the zoom)
     *
     * @param ship The player ship.
     * @return The offset.
     */
    public static Vector2f getHUDOffset(ShipAPI ship) {
        ViewportAPI viewport = Global.getCombatEngine().getViewport();
        float mult = viewport.getViewMult();

        return new Vector2f((int) (-ship.getCollisionRadius() / mult),
                (int) (ship.getCollisionRadius() / mult));
    }


    /**
     * Get the UI Element Offset.
     * (Depends on the weapon groups and wings present)
     *
     * @param ship    The player ship.
     * @param variant The variant of the ship.
     * @param vec1    Vector2f used if there are no wing and less than 2 weapon groups.
     * @param vec2    Vector2f used if there are wings but less than 2 weapon groups.
     * @return the offset.
     */
    private static Vector2f getUIElementOffset(ShipAPI ship, ShipVariantAPI variant, Vector2f vec1, Vector2f vec2) {
        int numEntries = 0;
        final java.util.List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
        final List<WeaponAPI> usableWeapons = ship.getUsableWeapons();
        for (WeaponGroupSpec group : weaponGroups) {
            final Set<String> uniqueWeapons = new HashSet<>(group.getSlots().size());
            for (String slot : group.getSlots()) {
                boolean isUsable = false;
                for (WeaponAPI weapon : usableWeapons) {
                    if (weapon.getSlot().getId().contentEquals(slot)) {
                        isUsable = true;
                        break;
                    }
                }
                if (!isUsable) {
                    continue;
                }
                String id = Global.getSettings().getWeaponSpec(variant.getWeaponId(slot)).getWeaponName();
                if (id != null) {
                    uniqueWeapons.add(id);
                }
            }
            numEntries += uniqueWeapons.size();
        }
        if (variant.getFittedWings().isEmpty()) {
            if (numEntries < 2) {
                return vec1;
            }
            return new Vector2f(30f + ((numEntries - 2) * 13f), 18f);
        } else if (variant.getFittedWings().size() < 4) {
            if (numEntries < 2) {
                return vec2;
            }

            return new Vector2f(59f + ((numEntries - 2) * 13f), 18f);
        } else {
            if (numEntries < 2) {
                return vec2;
            }
            return new Vector2f(59f + ((numEntries - 2) * 13f), 76f);
        }
    }


    /**
     * GL11 to start, when you want render text of Lazyfont.
     */
    public static void openGL11ForText() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
        GL11.glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * GL11 to close, when you want render text of Lazyfont.
     */
    public static void closeGL11ForText() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
