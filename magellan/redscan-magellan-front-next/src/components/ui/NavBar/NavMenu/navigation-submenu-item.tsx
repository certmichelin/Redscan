'use client'

import {NavigationMenuLink, navigationMenuTriggerStyle} from "@components/ui/NavBar/NavMenu/navigation-menu";
import Link from "next/link";
import React from "react";
import {cn} from "@lib/utils";

interface NavigationSubmenuItemProps {
    className?: string,
    title: string,
    href: string
}

export function NavigationSubmenuItem({className, title, href}: NavigationSubmenuItemProps) {
    return (
        <li>
            <Link href={href} legacyBehavior passHref>
                <NavigationMenuLink className={cn(
                    navigationMenuTriggerStyle(),
                    "block select-none space-y-1 rounded-md p-3 leading-none no-underline outline-none transition-colors hover:bg-accent hover:text-accent-foreground focus:bg-accent focus:text-accent-foreground",
                    className
                )}>
                    <div className="text-sm font-medium leading-none">{title}</div>
                </NavigationMenuLink>
            </Link>
        </li>
    )
}