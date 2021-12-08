<script>
    import DiscordButton from './discord/DiscordButton.svelte';
    import gitLogo from '$lib/github-logo.svg';
</script>

<footer>
    <hr>
    <div class="links">
        <a class="button no-background" href="https://github.com/Travja">
            <img src="{gitLogo}" alt="GitHub"/>
        </a>
        <a class="button" href="mailto:the.only.t.craft@gmail.com">Contact Me!</a>
        <DiscordButton fontSize="1"/>
    </div>
    <div class="copy">&copy;2021 Travja</div>
</footer>

<style>
    footer {
        position: relative;
        background: var(--bg-color);
        z-index: 4;
    }

    hr {
        margin: 0 30px;
    }

    .links {
        display: flex;
        align-items: stretch;
        justify-content: center;
    }

    .links > * {
        margin: 10px;
        padding: 10px;
    /*    border-radius: 6px;*/
    /*    background-color: rgba(0, 0, 0, .3);*/
        display: flex;
        align-items: center;
    }

    .links > .no-background {
        background-color: unset;
    }

    .copy {
        text-align: center;
        font-size: 0.9em;
        margin-bottom: 10px;
    }
</style>