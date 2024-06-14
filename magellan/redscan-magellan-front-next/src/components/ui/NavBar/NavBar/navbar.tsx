'use client'

import {
    NavigationMenu,
    NavigationMenuContent, NavigationMenuIndicator,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
    NavigationMenuTrigger,
    navigationMenuTriggerStyle, NavigationMenuViewport
} from "@components/ui/NavBar/NavMenu/navigation-menu";
import {NavBrand} from "@components/ui/NavBar/NavBrand/navbrand";
import {NavigationSubmenuItem} from "@components/ui/NavBar/NavMenu/navigation-submenu-item";
import Link from "next/link";
import {Separator} from "@components/ui/Separator/separator";

export function Navbar() {
    return (
        <div className={"flex flex-col"}>
            <div className={"flex flex-row justify-self-center"}>
                <NavBrand className={"flex-grow ml-32 mt-8 mb-4"} />
                <NavigationMenu className={"flex-auto self-end mr-16 mb-4"}>
                    <NavigationMenuList>
                        <NavigationMenuItem>
                            <Link href="/brands" legacyBehavior passHref>
                                <NavigationMenuLink className={navigationMenuTriggerStyle()}>
                                    Brands
                                </NavigationMenuLink>
                            </Link>
                        </NavigationMenuItem>
                        <NavigationMenuItem>
                            <Link href="/masterdomains" legacyBehavior passHref>
                                <NavigationMenuLink className={navigationMenuTriggerStyle()}>
                                    MasterDomains
                                </NavigationMenuLink>
                            </Link>
                        </NavigationMenuItem>
                        <NavigationMenuItem>
                            <Link href="/ipranges" legacyBehavior passHref>
                                <NavigationMenuLink className={navigationMenuTriggerStyle()}>
                                    IP Ranges
                                </NavigationMenuLink>
                            </Link>
                        </NavigationMenuItem>
                        <NavigationMenuItem>
                            <NavigationMenuTrigger>Other</NavigationMenuTrigger>
                            <NavigationMenuContent>
                                <ul className="grid w-[400px] gap-3 p-4">
                                    <NavigationSubmenuItem
                                        href={'/domains'}
                                        title={'Domains'}
                                        className={"min-w-full"}
                                    />
                                    <NavigationSubmenuItem
                                        href={'/ips'}
                                        title={'IPs'}
                                        className={"min-w-full"}
                                    />
                                </ul>
                            </NavigationMenuContent>
                        </NavigationMenuItem>
                    </NavigationMenuList>
                    <NavigationMenuIndicator />
                </NavigationMenu>
            </div>
            <Separator orientation={"horizontal"}/>
        </div>
    )
}