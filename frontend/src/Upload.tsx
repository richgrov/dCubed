import { useCallback, useEffect, useRef, useState } from "react";
import { Button, IconButton } from "./Button";
import { useDropzone } from "react-dropzone";

import { COLORS, Cube } from "./model";

import { CameraIcon, CheckIcon, XMarkIcon } from "@heroicons/react/16/solid";
import phone from "./assets/phone.png";
import loadingAnimation from "./assets/loading.gif";

const NETWORK_ERROR = "Network error. Check connection and try again.";
const SCAN_ERROR =
	"Couldn't find a cube in this image. Ensure it is centered in the frame and has 3 sides clearly visible.";
const UPLOAD_ERROR =
	"Wrong image upload. Double-check file type and try again.";
const SERVER_ERROR = "Internal error. Please try again later.";

export type CompletionCallback = (cube: Cube, sessionId: string) => void;

type ScanResponse = {
	cube: Partial<Cube>;
	sessionId: string;
};

async function scan(
	file: Blob,
	sessionId: string | undefined
): Promise<ScanResponse> {
	const formData = new FormData();
	formData.append("photo", file);

	let url = import.meta.env.VITE_BACKEND_URL + "/scan-photo";
	if (typeof sessionId !== "undefined") {
		url += "?session=" + encodeURIComponent(sessionId);
	}

	let response: Response;
	try {
		response = await fetch(url, { method: "POST", body: formData });
	} catch (e) {
		console.error("Fetch error: " + e);
		throw new Error(NETWORK_ERROR);
	}

	if (response.status === 422) {
		throw new Error(SCAN_ERROR);
	}

	if (response.status === 400) {
		throw new Error(UPLOAD_ERROR);
	}

	if (response.status !== 200) {
		throw new Error(SERVER_ERROR);
	}

	const responseData = await response.json();

	return {
		cube: responseData.sides,
		sessionId: responseData.sessionId,
	};
}

type UploadMode = "upload" | "camera";

export default function Scanner(props: { onComplete: CompletionCallback }) {
	const [loading, setLoading] = useState(false);
	const [sides, setSides] = useState<Partial<Cube>>({});
	const sessionId = useRef<string | undefined>();
	const [errorDialog, setErrorDialog] = useState<JSX.Element>();
	const [uploadMode, setUploadMode] = useState<UploadMode>("upload");

	useEffect(() => {
		if (Object.keys(sides).length === 6) {
			props.onComplete(sides as Cube, sessionId.current!);
		}
	}, [sides]);

	function toggleUploadMode() {
		setUploadMode((mode) => (mode === "upload" ? "camera" : "upload"));
	}

	async function onImageUpload(file: Blob) {
		setLoading(true);

		try {
			const result = await scan(file, sessionId.current);
			sessionId.current = result.sessionId;

			setSides((sides) => {
				return { ...sides, ...result.cube };
			});
		} catch (e: any) {
			setErrorDialog(
				<div className="mb-5 max-w-3xl rounded-xl bg-red-600 p-5 text-2xl text-white shadow-[black_10px_10px]">
					{e.message}
				</div>
			);
		} finally {
			setLoading(false);
		}
	}

	const uploadToggle =
		uploadMode === "upload" ? "Use Camera Instead" : "Upload Existing Image";

	return (
		<div className="flex h-full flex-col items-center">
			{uploadMode === "upload" ? (
				<Uploader
					onScan={onImageUpload}
					sides={sides}
					errorDialog={errorDialog}
				/>
			) : (
				<CameraCapture onScan={onImageUpload} />
			)}
			<div className="w-full border-t-4 border-dashed border-black p-5 text-center">
				<Button onClick={toggleUploadMode}>{uploadToggle}</Button>
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

type UploaderProps = {
	onScan: (file: Blob) => void;
	sides: Partial<Cube>;
	errorDialog: JSX.Element | undefined;
};

export function Uploader(props: UploaderProps) {
	const onDrop = useCallback(async (accepted: File[]) => {
		const file = accepted[0];
		props.onScan(file);
	}, []);

	const { getRootProps, getInputProps } = useDropzone({
		accept: {
			"image/png": [".png"],
			"image/jpeg": [".jpeg"],
		},
		onDrop,
		maxFiles: 1,
	});

	return (
		<div
			{...getRootProps()}
			className="flex w-full flex-1 cursor-pointer flex-col items-center justify-center gap-5 text-center"
		>
			<input {...getInputProps()} />

			{typeof props.errorDialog !== undefined && props.errorDialog}

			<img
				src={phone}
				alt="A picture of a Rubik's cube on a flat surface"
				className="rounded-xl border-4 border-dashed border-black"
			/>

			<ScannedColors cube={props.sides} />

			<p className="max-w-lg text-2xl">
				Photograph a picture of your cube on a flat surface, where 3 sides
				are clearly visible.
			</p>
		</div>
	);
}

function CameraCapture(props: { onScan: (file: Blob) => void }) {
	const videoRef = useRef<HTMLVideoElement | undefined>(undefined);

	useEffect(() => {
		navigator.mediaDevices
			.getUserMedia({
				video: true,
			})
			.then((stream) => {
				const video = videoRef.current!;
				video.srcObject = stream;
				video.play();
			});
	}, []);

	function takePhoto() {
		const video = videoRef.current!;

		const canvas = new OffscreenCanvas(video.videoWidth, video.videoHeight);
		const ctx = canvas.getContext("2d")!;
		ctx.drawImage(video, 0, 0);

		canvas.convertToBlob().then((blob) => {
			if (blob === null) {
				console.error("blob was null");
				return;
			}
			props.onScan(blob!);
		});
	}

	return (
		<div className="flex w-full flex-1 justify-center rounded-xl bg-gray-500">
			<div className="absolute left-14 top-10">
				<Button onClick={takePhoto} className="flex items-center">
					<CameraIcon width={48} />
					<p className="text-lg">Take Photo</p>
				</Button>
			</div>
			<video ref={videoRef} className="h-full"></video>
		</div>
	);
}

function ScannedColors(props: { cube: Partial<Cube> }) {
	return (
		<div className="flex gap-3">
			{COLORS.map((key) => {
				const css = { backgroundColor: key.toLowerCase() };
				const isScanned =
					typeof props.cube[key as keyof Cube] !== "undefined";
				return (
					<div
						className="h-10 w-10 rounded-full border-4 border-black shadow-[black_4px_4px]"
						style={css}
						key={key}
					>
						{isScanned ? <CheckIcon /> : <XMarkIcon />}
					</div>
				);
			})}
		</div>
	);
}
