<script>
    import {createEventDispatcher, onMount} from "svelte";
    import {loginState} from "$lib/variables";
    import {parseJWT} from "$lib/util";

    let mounted = false;
    let loggedIn = loginState.loggedIn;
    let username;
    let text;

    const dispatch = createEventDispatcher();

    $: if (mounted && $loggedIn) text = username = parseJWT().sub;

    onMount(() => {
        mounted = true;
    });
</script>

{#if $loggedIn && username}
    <li class="logout" on:click={() => dispatch("logout")}
        on:mouseover={() => text = "Log Out"}
        on:focus={() => text = "Log Out"}
        on:blur={() => text = username}
        on:mouseout={() => text = username}>{text}</li>
{:else}
    <li class="login" on:click>Log In</li>
{/if}

<style>
    .login, .logout {
        background: rgba(0, 0, 255, 0.2);
    }

    li {
        position: relative;
        height: 100%;
        display: flex;
        height: 100%;
        width: 100%;
        box-sizing: border-box;
        text-align: center;
        align-items: center;
        justify-content: center;
        padding: 0.5em;
        color: var(--heading-color);
        font-weight: 700;
        font-size: 0.8rem;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        text-decoration: none;
    }

    li:hover {
        cursor: pointer;
        color: var(--accent-color);
    }
</style>