export const COLORS = ["WHITE", "RED", "ORANGE", "YELLOW", "GREEN", "BLUE"];
export type Color = "WHITE" | "RED" | "ORANGE" | "YELLOW" | "GREEN" | "BLUE";

export type Cube = { [K in Color]: Color[] };

export type AppState = {
  cube: Cube;
  sessionId: string;
};

export type Move = {
  side: string;
  clockwise: boolean;
};

export class SolveStageIndices {
  public whiteCross = 0;
  public whiteCorners = 0;
  public secondLayer = 0;
  public yellowCross = 0;
  public yellowEdges = 0;
  public positionYellowCorners = 0;
  public orientYellowCorners = 0;
}
