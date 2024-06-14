import type { Metadata } from "next";
import { inter } from "@components/ui/fonts";
import "@styles/globals.css";
import {Navbar} from "@components/ui/NavBar/NavBar/navbar";

export const metadata: Metadata = {
  title: "Magellan",
  description: "Magellan",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${inter.className} antialiased`}>
        <header>
          <Navbar></Navbar>
        </header>
        <main>
          {children}
        </main>
        <footer>

        </footer>
      </body>
    </html>
  );
}
