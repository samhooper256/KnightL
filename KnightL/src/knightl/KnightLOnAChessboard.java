package knightl;

import java.util.*;

/**
 * This class provides the algorithm to calculate the fastest path for the knight.
 * It uses a BFS search.
 * 
 * @author Sam Hooper
 *
 */
class KnightLOnAChessboard {
	/*
	 * Calculates the shortest path to the given square, returned as an {@code ArrayList} of length 2 int arrays,
	 * each int array containing the row (slot 0) and column (slot 1) of the tiles the knight must visit
	 * on the path. The list contains the tiles on the path starting from the start point and ending at
	 * the end point. The first index of the {@code ArrayList} will contain the starting point.
	 * 
	 * Returns {@code null} if a path is not possible.
	 * 
	 *  @param a one component of the knight's movement
	 *  	Must be >= 0. a and b cannot both be zero.
	 *  @param b the other component of the knight's movement
	 *  	Must be >= 0. a and b cannot both be zero.
	 *  @param startRow the row of the knight's starting position
	 *  	Must be a valid location on a board of BOARD_SIZE rows and columns.
	 *  @param startCol the column of the knight's starting position
	 *  	Must be a valid location on a board of BOARD_SIZE rows and columns.
	 *  @param destRow the row of the knight's destination tile
	 *  	Must be a valid location on a board of BOARD_SIZE rows and columns.
	 *  @param destCol the column of the knight's destination tile
	 *  	Must be a valid location on a board of BOARD_SIZE rows and columns.
	 *  @param BOARD_SIZE the size of the chessboard
	 *  	Must be > 0.
	 *  @param blockedTiles a boolean[][] where true values indicate a blocked tile at those coordinates.
	 *  	Must be a square matrix with >= BOARD_SIZE rows.
	 *  @throws IllegalArgumentException if any of the parameters are invalid.
	 *  @return an {@code ArrayList} containing the locations of the tiles in the shortest path
	 *  or {@code null} if no path exists.
	 * */
	static ArrayList<int[]> getMovesBFS(int a, int b, int startRow, int startCol, int destRow, int destCol, final int BOARD_SIZE, boolean[][] blockedTiles){
		if(		BOARD_SIZE <= 0 || (a < 0 || b < 0 || (a == 0 && b == 0)) || startRow < 0 || startRow >= BOARD_SIZE || startCol < 0 || startCol >= BOARD_SIZE ||
				destRow < 0 || destRow >= BOARD_SIZE || blockedTiles.length <= 0 || blockedTiles.length != blockedTiles[0].length ||
				blockedTiles.length < BOARD_SIZE) {
			throw new IllegalArgumentException("Bad arguments passed to getMovesBFS. args were:" +
				String.format("a=%d,b=%d,start=(%d,%d),dest=(%d,%d),boardsize=%d,blockedTiles=%s%n",
						a,b,startRow,startCol,destRow,destCol,BOARD_SIZE,Arrays.deepToString(blockedTiles)));
		}
		class Move{
			int r, c;
			Move parent;
			Move(int a, int b, Move p) {
				r = a;
				c = b;
				parent = p;
			}
		}
		int[][] moves = {
				{a,b},
				{a,-b},
				{-a,b},
				{-a,-b},
				{b,a},
				{b,-a},
				{-b,a},
				{-b,-a}
		};
		Queue<Move> spots = new LinkedList<>();
		spots.add(new Move(startRow, startCol, null));
		Move path = null;
		boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
		while(!spots.isEmpty()) {
			Move spot = spots.remove();
			int r = spot.r, c = spot.c;
			if(r == destRow && c == destCol) {
				path = spot;
				break;
			}
			for(int[] moveOption : moves) {
				int newR = r + moveOption[0];
				int newC = c + moveOption[1];
				if(newR >= 0 && newR < BOARD_SIZE && newC >= 0 && newC < BOARD_SIZE &&
						!visited[newR][newC] && !blockedTiles[newR][newC]) {
					spots.add(new Move(newR, newC, spot));
					visited[newR][newC] = true;
				}
			}
		}
		
		if(path == null) { //not possible
			return null;
		}
		
		ArrayList<int[]> end = new ArrayList<>();
		while(path != null) {
			end.add(new int[] {path.r,path.c});
			path = path.parent;
		}
		for(int i = 0; i < end.size() / 2; i++) { //reverse the list so that it appears in order
			int[] temp = end.get(i);
			end.set(i, end.get(end.size() - i - 1));
			end.set(end.size() - i - 1, temp);
		}
		return end;
	}
}
