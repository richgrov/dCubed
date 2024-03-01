import { useCallback, useRef, useState } from "react";
import { Button } from "./Button";
import { useDropzone } from "react-dropzone";

import loadingAnimation from "./assets/loading.gif";
import phone from "./assets/phone.png";
import Visual, { CubeInfo } from "./Visual";

export default function Solver() {
  const sessionId = useRef<string | undefined>(undefined);
  const [loading, setLoading] = useState(false);
  const [cubeInfo, setCubeInfo] = useState<CubeInfo | undefined>(undefined);

  const onPhoto = async (file: File) => {
    setLoading(true);

    const formData = new FormData();
    formData.append("photo", file);

    let url = import.meta.env.VITE_BACKEND_URL + "/scan-photo";
    if (typeof sessionId.current !== "undefined") {
      url += "?" + new URLSearchParams({ session: sessionId.current });
    }

    try {
      const response = await fetch(url, { method: "POST", body: formData });
      if (response.status !== 200) {
        return;
      }

      const responseData = await response.json();
      sessionId.current = responseData.sessionId;

      if (Object.keys(responseData.sides).length === 6) {
        setCubeInfo({
          sides: responseData.sides,
          session: sessionId.current!,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center gap-20 py-20">
      <h1 className="text-5xl">Click or Drop File Below to Scan</h1>
      <div className="relative h-[80vh] w-4/5 rounded-2xl border-4 border-black">
        {typeof cubeInfo !== "undefined" ? (
          <Visual cubeInfo={cubeInfo} />
        ) : (
          <InputSetup onPhoto={onPhoto} />
        )}
        {loading ? (
          <div className="rounded-1xl absolute bottom-0 left-0 right-0 top-0 flex items-center justify-center bg-[rgba(0,_0,_0,_0.5)]">
            <img src={loadingAnimation} alt="Loading Animation" />
          </div>
        ) : (
          ""
        )}
      </div>
    </div>
  );
}

function InputSetup(props: { onPhoto: (file: File) => void }) {
  const onDrop = useCallback((accepted: File[]) => {
    props.onPhoto(accepted[0]);
  }, []);

  const handleCamera = () => {};
  const { getRootProps, getInputProps } = useDropzone({
    accept: {
      "image/png": [".png"],
      "image/jpeg": [".jpeg"],
    },
    onDrop,
    maxFiles: 1,
  });

  return (
    <div className="flex h-full flex-col items-center">
      <div
        {...getRootProps()}
        className="flex w-full flex-1 cursor-pointer flex-col items-center justify-center gap-5 border-b-4 border-dashed border-black"
      >
        <input {...getInputProps()} />

        <img
          src={phone}
          alt="A picture of a Rubik's cube on a flat surface"
          className="rounded-xl border-4 border-dashed border-black"
        />
        <p className="max-w-lg text-center text-2xl">
          Photograph a picture of your cube on a flat surface, where 3 sides are
          clearly visible.
        </p>
      </div>
      <div className="p-5">
        <Button onClick={handleCamera}>Use Camera Instead</Button>
      </div>
    </div>
  );
}
