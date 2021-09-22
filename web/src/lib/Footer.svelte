<script>
    import DiscordButton from './discord/DiscordButton.svelte';
    import gitLogo from '$lib/github-logo.svg';
</script>

<footer>
    <hr>
    <div class="links">
        <a class="no-background" href="https://github.com/Travja">
            <img src="{gitLogo}" alt="GitHub"/>
        </a>
        <a href="#">Contact Me!</a>
        <DiscordButton fontSize="1"/>
    </div>
    <div class="copy">&copy; 2021 Travja</div>
</footer>

<style>
    footer {
        margin: 0 30px;
    }
    .links {
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .links > * {
        margin: 10px;
        padding: 10px;
        border-radius: 6px;
        background-color: rgba(0, 0, 0, .3);
        display: flex;
        align-items: center;
    }

    .links > .no-background {
        padding: 0;
        background-color: unset;
    }

    a {
        color: var(--fg-color);
    }

    a:hover {
        text-decoration: none;
    }

    .copy {
        text-align: center;
        font-size: 0.9em;
        margin-bottom: 10px;
    }
</style>