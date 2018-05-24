import java.applet.Applet;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Vector;

@SuppressWarnings("serial")
public class V extends Applet implements Runnable {
	
	public void start() {
		new Thread( this ).start();
	}
	
	public void run() {
		
		final boolean TEST = false;
		
		// ==============================================================================
		// GAME WIDE CONSTANTS
		// ==============================================================================
		
		final float HALF_PI = 1.5707963267948966192313216916398f;
		
		final int KEY_LEFT          = 0;
		final int KEY_RIGHT         = 1;
		final int KEY_UP            = 2;
		final int KEY_DOWN          = 3;
		//final int KEY_CONTROL       = 4;
		final int KEY_MOUSE_LEFT    = 5;
		final int KEY_MOUSE_RIGHT   = 6;
		final int SCENE_WIDTH       = 800;
		final int SCENE_HEIGHT      = 600;
		// Number of points defining a surface
		final int SURFACE_POINTS    = 4;
		
		final int MAX_HEALTH        = 200;
		final int MAX_FIRE_COOLDOWN = 8;
		
		final float SURFACE_WALL  = 0;
		final float SURFACE_HEART = 1;
		
		final float[] DUMMY_POINT = new float[ 3 ];
		
		final Font font = new Font( "Times New Roman", Font.PLAIN, 0 );
		
// =====================================================================================================================================
// GAME MODEL AND STATE (MODEL OF MVC)
// =====================================================================================================================================
		
		// 2D array of map (first index: z, second: x)
		char[][] map = null;
		// Vector of surfaces which are arrays of points which are array of coordinates (x, y, y)
		// Each surface is specified with SURFACE_POINTS points
		final Vector< float[][] > surfaces = new Vector< float[][] >();
		
		// ==============================================================================
		// GAME VARIABLES
		// ==============================================================================
		
		// Set up the graphics stuff, double-buffering.
		final BufferedImage buffer = new BufferedImage( SCENE_WIDTH, SCENE_HEIGHT, BufferedImage.TYPE_INT_RGB );
		final Graphics2D    g2     = (Graphics2D) buffer.getGraphics();
		
		// Uncomment to turn on antialiasing
		//g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		
		// Iteration counter, tells which iteration are we in.
        int       iteration = -1;		
		// Position of the player on the map (x, y, alpha)
        float     px = 0, pz = 0, pa = 0;
		// Time of the last iteration
        long      lastTime = 0;
        // Last mouse x coordinate
        int       mouseX = -1;
        // Health of the player
        int       health = 0;
        // How long 'till we can fire again
        int       fireCooldown = 0;
        // General variables
		int       i, j, k, l;
		float     surface[][], point[], x = 0, z = 0, d;
		int[]     xPoints = new int[ SURFACE_POINTS ], yPoints = new int[ SURFACE_POINTS];
		
		while ( true ) try {
			if ( System.nanoTime() - lastTime > 40000000 ) { // 25 fps
				lastTime = System.nanoTime();
				
// =====================================================================================================================================
// GAME CONTROLLER AND LOGIC (CONTROLLER OF MVC)
// =====================================================================================================================================
				if ( iteration < 0 ) {
					// INIT NEW GAME
					
					// Create surfaces
					surfaces.clear();
					
					health       = MAX_HEALTH / 2;
					fireCooldown = 0;
					// Map elements: 'a'-'p' wall with surface definitions, ' ' empty, 'H' heart, 'S' start location
					// Init map
					final String[] rows = "aeeeeeeeeeeeeeeeeeaeaeeeeeeeeeeeeeeeaeeea;c                 kHk               k   i;afh    nfffd nfd  oSo  p p p l   p  o   i;c          k   k             k          i;c          k   o  p p  nffh  k   p  l   i;c  l nffd  k                 k      k   i;c  k    k  k      nffffh  l  ifffd  k   i;c  k    k  ifffh          k  o H o  o   i;c  k    k  o      ndHHjh  k             i;c  k    k      nd  o  o   iffffh jfffd  i;c  mffh o  p    k         k      k   k  i;c              nefffffh   k  nfffg l mffa;cH         l              k        k    i;abbbbbbbbbbabbbbbbbbbbbbbbabbbbbbbbabbbba".split( ";" );
					map = new char[ rows.length ][];
					for ( i = 0; i < rows.length; i++ ) {
						map[ i ] = rows[ i ].toCharArray();
						for ( j = 0; j < map[ i ].length; j++ ) {
							k = map[ i ][ j ];
							if ( k >= 'a' ) {
								// Wall with surface definitions
								k -= 'a';
								// Point order of defining surfaces: if we're facing it:
								// 1st: left top, 2nd: right top, 3rd: right bottom, 4th: left bottom
								// TODO These checks can be commented out (but then about 2-3 times more surfaces will be generated which will never be visible!); gain: about 56 bytes!
								if ( ( k & 0x01 ) > 0 )
									surfaces.add( new float[][] { { j, 1, i }, { j+1, 1, i }, { j+1, 0, i }, { j, 0, i }, { SURFACE_WALL } } );
								if ( ( k & 0x02 ) > 0 )
									surfaces.add( new float[][] { { j+1, 1, i }, { j+1, 1, i+1 }, { j+1, 0, i+1 }, { j+1, 0, i }, { SURFACE_WALL } } );
								if ( ( k & 0x04 ) > 0 )
									surfaces.add( new float[][] { { j+1, 1, i+1 }, { j, 1, i+1 }, { j, 0, i+1 }, { j+1, 0, i+1 }, { SURFACE_WALL } } );
								if ( ( k & 0x08 ) > 0 )
									surfaces.add( new float[][] { { j, 1, i+1 }, { j, 1, i }, { j, 0, i }, { j, 0, i+1 }, { SURFACE_WALL } } );
							}
							else if ( k == 'S' ) {
								// Start location
								px = j + 0.5f;
								pz = i + 0.5f;
								pa = HALF_PI; // This is the facing direction on start, changing this would require to rotate all surfaces (which we don't want obviously)
							} else if ( k == 'H' ) {
								// Hearts
								// Although only the first point is used as the location, the second point is required to be the same
								// for proper distance sorting (which uses the first 2 points)
								surfaces.add( new float[][] { { j + 0.5f, 0, i + 0.5f }, { j + 0.5f, 0, i + 0.5f }, DUMMY_POINT, DUMMY_POINT, { SURFACE_HEART } } );
							}
						}
					}
					
					// Shift surfaces based on the start location
					for ( i = 0; i < surfaces.size(); i++ ) {
						surface = surfaces.get( i );
						for ( j = 0; j < SURFACE_POINTS; j++ ) {
							surface[ j ][ 0 ] -= px;
							surface[ j ][ 1 ] -= 0.5;
							surface[ j ][ 2 ] -= pz;
						}
					}
				}
				
				// CALCULATE THE NEXT ITERATION
				iteration++;
				
				// Handle keys
				if ( fireCooldown == 0 ) {
					if ( q[ KEY_MOUSE_LEFT ] )
						fireCooldown = MAX_FIRE_COOLDOWN;
				}
				else
					fireCooldown--;
				float dx, sinAlpha = 0, cosAlpha = 0, mouseDx = 0;
				// Strafe
				dx = ( q[ KEY_LEFT ] ? 0.08f : 0 ) + ( q[ KEY_RIGHT ] ? -0.08f : 0 );
				if ( m >= 0 ) {
					if ( !q[ KEY_MOUSE_RIGHT ] ) {
						mouseDx = mouseX < 0 ? 0 : ( m - mouseX ) * 0.01f;
						// Turn
						if ( m != mouseX ) {
							sinAlpha = (float) Math.sin( mouseDx );
							cosAlpha = (float) Math.cos( mouseDx );
						}
					}
					mouseX = m;
				}
				float dz = ( q[ KEY_UP ] ? -0.08f : 0 ) + ( q[ KEY_DOWN ] ? 0.08f : 0 );
				// Perform step
				// Check if there is a wall there
				x = px - (float) Math.cos( pa ) * dz + (float) Math.cos( pa + HALF_PI ) * dx;
				z = pz - (float) Math.sin( pa ) * dz + (float) Math.sin( pa + HALF_PI ) * dx;
				k = 1; // OK
				// A little step to each side (enforce a little space between the player and the walls)
				for ( i = -1; i <= 1; i++ )
					for ( j = -1; j <= 1; j++ )
						if ( map[ (int) ( z + i / 10f ) ][ (int) ( x + j / 10f ) ] >= 'a' )
							k = 0;
				if ( k == 1 ) {
					px = x;
					pz = z;
				}
				else {
					dx = 0;
					dz = 0;
				}
				if ( mouseDx != 0 )
					pa -= mouseDx;
				// Update surfaces, items, monsters...
				for ( i = surfaces.size() - 1; i >= 0; i-- ) { // Downward is a must because surfaces might get removed in the cycle body!
					surface = surfaces.get( i );
					for ( j = 0; j < SURFACE_POINTS; j++ ) {
						surface[ j ][ 0 ] += dx;
						surface[ j ][ 2 ] += dz;
						if ( mouseDx != 0 ) {
							// Rotate
							x = surface[ j ][ 0 ];
							z = surface[ j ][ 2 ];
							surface[ j ][ 0 ] = x * cosAlpha - z * sinAlpha;
							surface[ j ][ 2 ] = x * sinAlpha + z * cosAlpha;
						}
					}
					if ( surface[ SURFACE_POINTS ][ 0 ] == SURFACE_HEART ) {
						// Pick up heart if we're close to it
						if ( surface[ 0 ][ 0 ] * surface[ 0 ][ 0 ] + surface[ 0 ][ 2 ] * surface[ 0 ][ 2 ] < 0.25f && health < MAX_HEALTH ) {
							if ( ( health += 50 ) > MAX_HEALTH )
								health = MAX_HEALTH;
							surfaces.remove( i );
						}
					}
				}
			}
			
// =====================================================================================================================================
// GAME VIEW (VIEW OF MVC)
// =====================================================================================================================================
			
			// Colors:
			// http://www.colorschemer.com
			// http://web.njit.edu/~kevin/rgb.txt.html
			// http://en.wikipedia.org/wiki/Web_colors
			
			// Draw ceiling and floor
			g2.setColor( new Color( 83, 156, 166 ) );
			g2.fillRect( 0, 0, SCENE_WIDTH, SCENE_HEIGHT/2 );
			g2.setColor( new Color( 178, 141, 38 ) );
			g2.fillRect( 0, SCENE_HEIGHT/2, SCENE_WIDTH, SCENE_HEIGHT/2 );
			
			// Draw the surfaces
			// First sort by distance (descending: farthest to the first place)
			// TODO: this only changes when turning (move this there)
			for ( i = 0; i < surfaces.size(); i++ ) {
				surface = surfaces.get( i );
				// Index of the farthest surface
				k = i;
				// Distance of the farthest surface
				z = ( surface[ 0 ][ 2 ] + surface[ 1 ][ 2 ] ) / 2;
				for ( j = i+1; j < surfaces.size(); j++ )
					if ( ( x = ( surfaces.get( j )[ 0 ][ 2 ] + surfaces.get( j )[ 1 ][ 2 ] ) / 2 ) > z ) {
						k = j;
						z = x;
					}
				surfaces.set( i, surfaces.get( k ) );
				surfaces.set( k, surface );
			}
			// Now draw surfaces (order is from farthest to nearest)
			for ( i = 0; i < surfaces.size(); i++ ) {
				surface = surfaces.get( i );
				if ( surface[ SURFACE_POINTS ][ 0 ] == SURFACE_WALL ) {
					point = surface[ 1 ];
					// 2 vectors defining the surface:
					final float[] v1 = { surface[ 0 ][ 0 ] - point[ 0 ], surface[ 0 ][ 1 ] - point[ 1 ], surface[ 0 ][ 2 ] - point[ 2 ] };
					final float[] v2 = { surface[ 2 ][ 0 ] - point[ 0 ], surface[ 2 ][ 1 ] - point[ 1 ], surface[ 2 ][ 2 ] - point[ 2 ] };
					// 1st atan2: vector pointing to the center of surface, 2nd atan2: x, z coordinates of the normal vector (y would be: v1[ 0 ]*v2[ 2 ] - v1[ 2 ]*v2[ 0 ] but it is not used)
					final float alpha = (float) ( Math.atan2( surface[ 0 ][ 0 ] + point[ 0 ], surface[ 0 ][ 2 ] + point[ 2 ] ) - Math.atan2( v1[ 1 ]*v2[ 2 ] - v1[ 2 ]*v2[ 1 ], v1[ 0 ]*v2[ 1 ] - v1[ 1 ]*v2[ 0 ] ) );
					// If z component of the normal vector of the surface is negative, it must not be rendered
					if ( alpha > HALF_PI || alpha < -HALF_PI )
						continue;
					l = 0; // Indication if the surface has visible points
					for ( k = 0; k < SURFACE_POINTS; k++ ) {
						point = surface[ k ];
						// If surface is behind us, it must not be rendered
						if ( point[ 2 ] > 0 )
							l = 1;
						if ( point[ 2 ] > 0 ) {
							// Projection
							xPoints[ k ] = SCENE_WIDTH /2 + (int) ( SCENE_HEIGHT * point[ 0 ] / point[ 2 ] );
							yPoints[ k ] = SCENE_HEIGHT/2 - (int) ( SCENE_HEIGHT * point[ 1 ] / point[ 2 ] );
						}
						else {
							// A very simple cut implementation (surface is cut with the z=0 plane)
							xPoints[ k ] = k % 3 == 0 ? 0 : SCENE_WIDTH;
							yPoints[ k ] = k < 2 ? 0 : SCENE_HEIGHT;
						}
					}
					if ( l == 0 )
						continue;
					
					z = 0.7f + (float) Math.abs( Math.cos( alpha ) ) * 0.3f
						// Fire light:
						+ ( ( d = surface[ 0 ][ 2 ] + surface[ 1 ][ 2 ] ) < 6 ? fireCooldown * ( 6 - d ) / 6 * 0.2f / MAX_FIRE_COOLDOWN : 0 );
					g2.setColor( new Color( (int) ( 133*z ), (int) ( 100*z ), (int) ( 81*z ) ) );
					g2.fillPolygon( xPoints, yPoints, SURFACE_POINTS );
				} else if ( surface[ SURFACE_POINTS ][ 0 ] == SURFACE_HEART ) {
					point = surface[ 0 ];
					if ( point[ 2 ] > 0 ) {
						g2.setColor( new Color( 220, 60, 60 ) );
						g2.setFont( font.deriveFont( 350 / point[ 2 ] ) );
						g2.drawString( "\u2665", SCENE_WIDTH /2 + (int) ( SCENE_HEIGHT * point[ 0 ] / point[ 2 ] ) - 105 / point[ 2 ], SCENE_HEIGHT/2 - (int) ( SCENE_HEIGHT * point[ 1 ] / point[ 2 ] ) );
					}
				}
			}
			
			// Crosshair
			g2.setColor( new Color( 255, 255, 255 ) );
			g2.setFont( font.deriveFont( 30f ) );
			g2.drawString( "\u00a4", SCENE_WIDTH/2-7, SCENE_HEIGHT/2+10 );
			
			// Weapon
			g2.setFont( font.deriveFont( 180f ) );
			g2.drawString( "\u2020", SCENE_WIDTH/2-44, SCENE_HEIGHT + 40 + 2*fireCooldown - MAX_FIRE_COOLDOWN );
			
			// Health bar
			g2.setColor( new Color( 220, 60, 60 ) );
			g2.drawRect( 8, SCENE_HEIGHT - 25, MAX_HEALTH + 3, 18 );
			g2.fillRect( 10, SCENE_HEIGHT - 23, health, 15 );
			
			//graphics.drawString( "Caught: ".concat( String.valueOf( fishesCaught ) ), 5, 14 );
			
			if ( TEST ) {
				g2.setColor( new Color( 255, 185, 15 ) );
				g2.drawLine( SCENE_WIDTH/2, 0, SCENE_WIDTH/2, SCENE_HEIGHT );
				g2.drawLine( 0, SCENE_HEIGHT/2, SCENE_WIDTH, SCENE_HEIGHT/2 );
				
				g2.setFont( font.deriveFont( 30f ) );
				g2.drawString( "\u06de \u06e9 \u1d25 \u2020 \u2021 \u20b4 \u263a \u263b \u263c \u2640 \u2642 \u2660 \u2663 \u2665 \u2666 ", 10, 30 );
			}
			
			// Draw the entire results on the screen.
			getGraphics().drawImage( buffer, 0, 0, null );
			
			Thread.yield();
			if ( !isActive() )
				return;
			// TODO remove this in production environment
			Thread.sleep( 1 );
		} catch ( Exception e ) {
			// TODO remove this in production environment
			e.printStackTrace();
		}
	}
	
	
	/** States of the 5 keys/buttons we use: LEFT, RIGHT, UP, DOWN, CONTROL, MOUSE_LEFT, MOUSE_RIGHT.
	 * +1 extra for handling other keys. */
	private final boolean[] q = new boolean[ 8 ];
	
	/** Mouse x coordinate. */
	private int m = -1;
	
	/**
	 * Handles the keyboard events controlling the game.
	 */
	@Override
	public boolean handleEvent( final Event event ) {
		q[ event.key == Event.LEFT ? 0 : event.key == Event.RIGHT ? 1 : event.key == Event.UP ? 2 : event.key == Event.DOWN ? 3 : 7 ]
				= event.id == Event.KEY_ACTION || event.id == Event.KEY_PRESS;
		q[ 4 ] = event.controlDown();
		
		if ( event.id == Event.MOUSE_MOVE || event.id == Event.MOUSE_DRAG )
			m = event.x;
		
		if ( event.id == Event.MOUSE_DOWN || event.id == Event.MOUSE_UP )
			q[ event.metaDown() ? 6 : 5 ] = event.id == Event.MOUSE_DOWN;
		
		return false;
	}
    
}
