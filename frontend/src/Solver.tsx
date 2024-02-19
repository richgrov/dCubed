import { useCallback, useRef, useState } from "react";
import Button from "./Button";
import { useDropzone } from "react-dropzone";

import loadingAnimation from "./assets/loading.gif";
import Visual from "./Visual";

export default function Solver() {
  const sessionId = useRef<string | undefined>(undefined);
  const [loading, setLoading] = useState(false);
  const [sides, setSides] = useState<{} | undefined>(undefined);

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
        setSides(responseData.sides);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center gap-20 py-20">
      <h1 className="text-5xl">Step 1: Scan Cube</h1>
      <div className="relative aspect-video w-3/5 rounded-2xl border-4 border-black">
        {typeof sides !== /* condition temporarily backwards */ "undefined" ? (
          <InputSetup onPhoto={onPhoto} />
        ) : (
          <Visual />
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
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
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
        className="flex w-full flex-1 cursor-pointer items-center justify-center border-b-4 border-dashed border-black"
      >
        <input {...getInputProps()} />
        <h1 className="text-3xl">
          {isDragActive
            ? "Drop Files Here"
            : "Click to Upload or Drop Photo Here"}
        </h1>
      </div>
      <div className="p-5">
        <Button onClick={handleCamera}>Use Camera Instead</Button>
      </div>
    </div>
  );
}
