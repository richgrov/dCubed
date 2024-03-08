import { ReactNode, MouseEventHandler } from "react";

export type ButtonProps = {
  onClick: MouseEventHandler;
  children: ReactNode;
  className?: string | undefined;
};

export function Button(props: ButtonProps) {
  return (
    <button
      onClick={props.onClick}
      className={
        "rounded-lg border-4 border-black bg-white p-4 shadow-[black_10px_10px] transition hover:shadow-[black_0_0] " +
        props.className
      }
    >
      {props.children}
    </button>
  );
}

export function IconButton(props: ButtonProps) {
  return (
    <button
      onClick={props.onClick}
      className="rounded-full border-4 border-black p-4 shadow-[black_10px_10px] transition hover:shadow-[black_0_0]"
    >
      {props.children}
    </button>
  );
}
