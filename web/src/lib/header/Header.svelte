<script>
    import {page} from '$app/stores';
    import logo from './logo.png';
    import gitLogo from '../github-logo.svg';
    import LoginButton from "$lib/header/LoginButton.svelte";
    import {loadJWT, verifyJWT} from "$lib/util";
    import LoginModal from "$lib/LoginModal.svelte";
    import {onMount} from "svelte";
    import {login, loginState, logout, title} from "$lib/variables";

    //Define nav bar routes here and which pages should be authenticated.
    let pages = [
        {
            path: "/",
            title: "Home"
        },
        {
            path: "/items",
            title: "Items"
        },
        {
            path: "/about",
            title: "About"
        },
        {
            path: "/scan",
            title: "Scan",
            auth: true
        },
        {
            path: "/list",
            title: "Shopping List",
            auth: true
        },
        {
            path: "/admin/approval",
            title: "Approval",
            auth: true,
            hidden: true
        }
    ];

    let requireLogin = false,
        showLoginModal = false,
        loggedIn = loginState.loggedIn,
        prompt = false;
    let mounted = false;
    $: if (mounted) {
        for (let pg of pages) {
            if ($page.path == pg.path) {
                if (pg.auth && !$loggedIn) {
                    requireLogin = true;
                    prompt = true;
                } else {
                    prompt = requireLogin = showLoginModal = false;
                }
                // goto("/login");
                break;
            }
        }
    }
    onMount(() => {
        if (verifyJWT()) {
            login(loadJWT());
        } else {
            logout();
        }

        mounted = true;
    });

    const handleLogin = e => {
        let detail = e.detail;
        if (detail.success)
            closeLoginModal();
    };

    const closeLoginModal = () => {
        prompt = showLoginModal = requireLogin = false;
    };

    let showNav = false;
    const showNavigation = e => {
        showNav = !showNav;
    };
</script>

<header>
    <div class="corner">
        <a href="/">
            <img alt="Crave" src={logo}/>
        </a>
    </div>

    <nav>
        <div class="show" on:click={showNavigation}>{$title}</div>
        <svg aria-hidden="true" viewBox="0 0 2 3">
            <path d="M0,0 L1,2 C1.5,3 1.5,3 2,3 L2,0 Z"/>
        </svg>
        <ul class:open={showNav} on:click={showNavigation}>
            {#each pages as pg}
                {#if !pg.hidden}
                    {#if pg.auth && $loggedIn || !pg.auth}
                        <a sveltekit:prefetch href={pg.path}
                           class:active={$page.path === pg.path}>
                            <li>{pg.title}</li>
                        </a>
                    {/if}
                {/if}
            {/each}
            <LoginButton
                    on:click={() => showLoginModal = true}
                    on:logout={logout}/>
        </ul>
        <svg aria-hidden="true" viewBox="0 0 2 3">
            <path d="M0,0 L0,3 C0.5,3 0.5,3 1,2 L2,0 Z"/>
        </svg>
    </nav>

    <div class="corner">
        <a href="https://github.com/Travja/Crave" target="_blank">
            <img alt="GitHub" src={gitLogo}/>
        </a>
    </div>

    {#if showLoginModal || requireLogin}
        <LoginModal on:closeModal={closeLoginModal} {prompt} {requireLogin}
                    on:login={handleLogin}/>
    {/if}
</header>

<style>
    header {
        position: sticky;
        top: 0;
        display: flex;
        justify-content: space-between;
        background: var(--bg-color);
        padding-bottom: 0.5em;
        z-index: 100;
        width: 100%;
        margin: 0 auto;
    }

    .corner {
        width: 3em;
        height: 3em;
    }

    .corner a {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
    }

    .corner img {
        width: 2em;
        height: 2em;
        object-fit: contain;
    }

    nav, .show {
        display: flex;
        justify-content: center;
        flex: 1;
        --background: #ccc;
        z-index: 100;
    }

    nav {
        flex-direction: column;
        align-items: center;
    }

    svg {
        display: none;
    }

    path {
        fill: var(--background);
    }

    ul {
        transition: max-height .5s ease-in-out;
        max-height: 0px;
        overflow: hidden;

        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        padding: 0;
        margin: 0;
        justify-content: center;
        align-items: center;
        list-style: none;
        background: var(--background);
        background-size: contain;
        width: 80%;
    }

    ul.open {
        max-height: 20rem;
    }

    .show {
        width: 80%;
        position: relative;
        display: flex;
        justify-content: center;
        align-items: center;
        background: var(--background);
        color: var(--heading-color);
        font-weight: 700;
        font-size: 0.8rem;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        text-decoration: none;
        box-sizing: border-box;
        padding: 1em;
        border-bottom: 2px solid var(--fg-color);
    }

    nav a {
        position: relative;
        display: flex;
        height: 100%;
        align-items: center;
        justify-content: center;
        padding: 0 1em;
        color: var(--heading-color);
        font-weight: 700;
        font-size: 0.8rem;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        text-decoration: none;
    }

    a.active::before {
        --size: 6px;
        content: '';
        width: 0;
        height: 0;
        position: absolute;
        top: calc(50% - var(--size));
        left: 0;
        border: var(--size) solid transparent;
        border-left: var(--size) solid var(--accent-color);
    }

    .transitions-enabled nav a {
        transition: color 0.2s linear;
    }

    a {
        color: var(--heading-color);
        text-decoration: none;
        width: 100%;
        box-sizing: border-box;
    }

    li {
        box-sizing: border-box;
        padding: 0.5em;
        width: 100%;
        height: 100%;
        display: flex;
        flex-grow: 1;
        text-align: center;
        align-items: center;
        justify-content: center;
        position: relative;
    }

    a:hover, nav li:hover {
        cursor: pointer;
        color: var(--accent-color);
    }

    @media only screen and (min-width: 768px) {
        .show {
            display: none;
        }

        nav {
            flex-direction: row;
        }

        svg {
            width: 2em;
            height: 3em;
            display: block;
        }

        ul {
            max-height: 3em;
            display: flex;
            flex-direction: row;
            position: relative;
            height: 3em;
            width: auto;
        }

        a.active::before {
            --size: 6px;
            content: '';
            width: 0;
            height: 0;
            position: absolute;
            top: 0;
            left: calc(50% - var(--size));
            border: var(--size) solid transparent;
            border-top: var(--size) solid var(--accent-color);
        }

        li {
            margin: 0;
        }
    }
</style>
