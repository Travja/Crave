<script>
    import {page} from '$app/stores';
    import logo from './logo.png';
    import gitLogo from '../github-logo.svg';
    import LoginButton from "$lib/header/LoginButton.svelte";
    import {loadJWT, verifyJWT} from "$lib/util";
    import LoginModal from "$lib/LoginModal.svelte";
    import {onMount} from "svelte";
    import {login, loginState, logout, variables} from "$lib/variables";

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
</script>

<header>
    <div class="corner">
        <a href="/">
            <img alt="Crave" src={logo}/>
        </a>
    </div>

    <nav>
        <svg aria-hidden="true" viewBox="0 0 2 3">
            <path d="M0,0 L1,2 C1.5,3 1.5,3 2,3 L2,0 Z"/>
        </svg>
        <ul>
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

    nav {
        display: flex;
        justify-content: center;
        --background: #ccc;
        z-index: 5000;
    }

    svg {
        width: 2em;
        height: 3em;
        display: block;
    }

    path {
        fill: var(--background);
    }

    ul {
        position: relative;
        padding: 0;
        margin: 0;
        height: 3em;
        display: flex;
        justify-content: center;
        align-items: center;
        list-style: none;
        background: var(--background);
        background-size: contain;
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

    .transitions-enabled nav a {
        transition: color 0.2s linear;
    }

    a {
        color: var(--heading-color);
        text-decoration: none;
    }

    a:hover, nav li:hover {
        cursor: pointer;
        color: var(--accent-color);
    }
</style>
