import {orbitron} from "@components/ui/fonts";
import {cn} from "@lib/utils";

interface NavBrandProps {
    className?: string
}

export function NavBrand({className}: NavBrandProps) {
    return (
        <span
            className={cn(
                `${orbitron.className} antialiased text-5xl font-bold`,
                className
            )}
        >
            MAGELLAN
        </span>
    );
}