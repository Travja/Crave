<!--suppress CssUnresolvedCustomProperty -->
<script>
    export let fontSize;
    import logo from './discord.svg';
</script>

<a href="https://discord.gg/VwrK3jj" style="--font-size:{fontSize}em;--img-size:{fontSize*1.2}em">
    <img src={logo} class="discord" alt="Discord"/>&nbsp;Join my&nbsp;<span
        class="highlight">Discord</span>
</a>

<style>
    a {
        font-size: var(--font-size);
        margin: 10px;
        padding: 10px;
        border-radius: 6px;
        background-color: rgba(0, 0, 0, .3);
        display: flex;
        align-items: center;
    }

    .discord {
        color: #66bb6a;
        width: var(--img-size);
    }

    .highlight {
        color: var(--link-color);
        font-weight: bold;
        text-decoration: underline;
    }

    a {
        color: var(--fg-color);
    }

    a:hover {
        text-decoration: none;
    }
</style>