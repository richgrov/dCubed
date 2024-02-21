import wavyCircle from "./assets/circle.png";
import cube from "./assets/cube.png";

export default function Hero() {
  return (
    <div className="flex justify-center border-b-4 border-b-black bg-amber-400 bg-[url('dots.png')] bg-contain bg-right bg-no-repeat py-48">
      <div className="max-w-[40%]">
        <h1 className="text-stroke-black mb-10 text-7xl text-transparent">
          Learn to Solve
          <span className="text-stroke-none text-black"> Your </span>
          Rubik's Cube
        </h1>
        <p className="inline rounded-full border-2 border-black p-5 text-xl">
          Skip the tutorials and get tailored instructions from your photos.
        </p>
      </div>

      <div className="relative self-center p-2">
        <img
          src={wavyCircle}
          alt="Wavy Circle"
          className="relative top-0 animate-[spin_30s_linear_infinite]"
        />
        <img src={cube} alt="Rubik's Cube" className="absolute top-0" />

        <svg
          width="100"
          height="100"
          xmlns="http://www.w3.org/2000/svg"
          className="absolute top-[-150px]"
        >
          <circle
            width="100"
            height="100"
            r="45"
            cx="50"
            cy="50"
            stroke="black"
            strokeWidth={5}
            fill="transparent"
          />
        </svg>

        <svg
          width="100"
          height="100"
          xmlns="http://www.w3.org/2000/svg"
          className="absolute bottom-[-150px] right-[-50px]"
        >
          <rect
            width="100"
            height="100"
            x="0"
            y="0"
            rx="5"
            ry="5"
            fill="black"
          />
        </svg>

        <svg
          width="100"
          height="100"
          xmlns="http://www.w3.org/2000/svg"
          className="absolute bottom-0 left-[-50px]"
        >
          <polygon points="0,0 100,100, 0,100" />
        </svg>
      </div>
    </div>
  );
}
