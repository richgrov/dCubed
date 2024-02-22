import { ReactNode, MouseEventHandler } from "react";

export type ButtonProps = {
  onClick: MouseEventHandler;
  children: ReactNode;
};

export function Button(props: ButtonProps) {
  return (
    <button
      onClick={props.onClick}
      className="rounded-lg border-4 border-black p-4 shadow-[black_10px_10px] transition hover:shadow-[black_0_0]"
    >
      {props.children}
    </button>
  );
}
