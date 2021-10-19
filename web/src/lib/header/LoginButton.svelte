<script>
    import {createEventDispatcher} from "svelte";
    import {loginState} from "$lib/variables";

    let loggedIn = loginState.loggedIn;

    const dispatch = createEventDispatcher();
</script>

{#if $loggedIn}
    <li class="logout" on:click={() => dispatch("logout")}>Log Out</li>
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
        align-items: center;
        padding: 0 1em;
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