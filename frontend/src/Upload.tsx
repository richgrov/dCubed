import { useCallback, useEffect, useRef, useState } from "react";
import { Button } from "./Button";
import { useDropzone } from "react-dropzone";

import { Cube } from "./model";

import phone from "./assets/phone.png";
import loadingAnimation from "./assets/loading.gif";

const SCAN_ERROR =
	"Couldn't scan a cube in this image. Ensure it is centered in the frame and has 3 sides clearly visible.";
const SERVER_ERROR = "Internal error. Please try again later.";

export type CompletionCallback = (cube: Cube, sessionId: string) => void;

type ScanResponse = {
	cube: Partial<Cube>;
	sessionId: string;
};

async function scan(
	file: File,
	sessionId: string | undefined
): Promise<ScanResponse> {
	const formData = new FormData();
	formData.append("photo", file);

	let url = import.meta.env.VITE_BACKEND_URL + "/scan-photo";
	if (typeof sessionId !== "undefined") {
		url += "?session=" + encodeURIComponent(sessionId);
	}

	try {
		const response = await fetch(url, { method: "POST", body: formData });
		if (response.status === 422) {
			throw new Error(SCAN_ERROR);
		}

		if (response.status !== 200) {
			throw new Error(SERVER_ERROR);
		}

		const responseData = await response.json();

		return {
			cube: responseData.sides,
			sessionId: responseData.sessionId,
		};
	} catch {
		throw new Error("Network error. Check connection and try again.");
	}
}

export default function Uploader(props: { onComplete: CompletionCallback }) {
	const [loading, setLoading] = useState(false);
	const [sides, setSides] = useState<Partial<Cube>>({});
	const sessionId = useRef<string | undefined>();

	const onDrop = useCallback(async (accepted: File[]) => {
		setLoading(true);
		const file = accepted[0];

		try {
			const result = await scan(file, sessionId.current);
			sessionId.current = result.sessionId;

			setSides((sides) => {
				return { ...sides, ...result.cube };
			});
		} finally {
			setLoading(false);
		}
	}, []);

	useEffect(() => {
		if (Object.keys(sides).length === 6) {
			props.onComplete(sides as Cube, sessionId.current!);
		}
	}, [sides]);

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
					Photograph a picture of your cube on a flat surface, where 3
					sides are clearly visible.
				</p>
			</div>
			<div className="p-5">
				<Button onClick={handleCamera}>Use Camera Instead</Button>
			</div>
			{loading ? (
				<div className="rounded-1xl absolute bottom-0 left-0 right-0 top-0 flex items-center justify-center bg-[rgba(0,_0,_0,_0.5)]">
					<img src={loadingAnimation} alt="Loading Animation" />
				</div>
			) : (
				""
			)}
		</div>
	);
}
