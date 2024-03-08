import { useState } from "react";

import Visual from "./Visual";
import Uploader from "./Upload";
import { AppState, Cube } from "./model";

export default function Solver() {
  const [appState, setAppState] = useState<AppState | undefined>(undefined);

  const onScanComplete = (scannedCube: Cube, session: string) => {
    setAppState({
      cube: scannedCube,
      sessionId: session,
    });
  };

  return (
    <div className="flex flex-col items-center gap-20 py-20">
      <h1 className="text-5xl">Drop File or Click Below to Get Started</h1>
      <div className="relative h-[80vh] w-4/5 rounded-2xl border-4 border-black">
        {typeof appState !== "undefined" ? (
          <Visual appState={appState} />
        ) : (
          <Uploader onComplete={onScanComplete} />
        )}
      </div>
    </div>
  );
}
