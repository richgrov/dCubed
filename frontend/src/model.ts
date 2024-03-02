export const COLORS = ["WHITE", "RED", "ORANGE", "YELLOW", "GREEN", "BLUE"];
export type Color = "WHITE" | "RED" | "ORANGE" | "YELLOW" | "GREEN" | "BLUE";

export type Cube = { [K in Color]: Color[] };

export type AppState = {
  cube: Cube;
  sessionId: string;
};
