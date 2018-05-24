import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;


public class MapEncoder {
	
	// 01234567890123456789012345
	// abcdefghijklmnopqrstuvwxyz
	
	public static void main( final String[] arguments ) throws Exception {
		final File[] mapFiles = new File( "maps" ).listFiles( new FilenameFilter() {
			@Override
			public boolean accept( final File dir, final String name ) {
				return name.toLowerCase().endsWith( ".dat" );
			}
		} );
		
		for ( final File file : mapFiles ) {
			final List< String > lineList = new ArrayList< String >();
			
			// Read the level from the file
			final BufferedReader input = new BufferedReader( new FileReader( file ) );
			String line;
			while ( ( line = input.readLine() ) != null )
				lineList.add( line.replace( '█', 'w' ).replace( '↑', 'S' ).replace( '♥', 'H' ) );
			
			// Create the map char matrix
			final char[][] map = new char[ lineList.size() ][];
			for ( int i = lineList.size() - 1; i >= 0; i-- )
				map[ lineList.size() - 1 - i ] = lineList.get( i ).toCharArray();
			
			// Determine what surfaces of the walls has to be created
			// And encode the map
			final StringBuilder mapBuilder = new StringBuilder();
			for ( int i = 0; i < map.length; i++ ) {
				if ( i > 0 )
					mapBuilder.append( ';' );
				for ( int j = 0; j < map[ i ].length; j++ )
					switch ( map[ i ][ j ] ) {
					case 'w': {
						int mask = 0;
						
						if ( i > 0 && map[ i - 1 ][ j ] != 'w' )
							mask |= 1;
						if ( j < map[ i ].length-1 && map[ i ][ j + 1 ] != 'w' )
							mask |= 2;
						if ( i < map.length-1 && map[ i + 1 ][ j ] != 'w' )
							mask |= 4;
						if ( j > 0 && map[ i ][ j - 1 ] != 'w' )
							mask |= 8;
						
						mapBuilder.append( (char) ( 'a' + mask ) );
						break;
					}
					default : mapBuilder.append(  map[ i ][ j ] ); break;
					}
			}
			
			System.out.println( file.getName() );
			System.out.println( mapBuilder );
			
			input.close();
		}
	}
	
}
