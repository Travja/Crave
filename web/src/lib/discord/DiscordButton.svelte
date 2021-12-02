<!--suppress CssUnresolvedCustomProperty -->
<script>
    export let fontSize;
    import logo from './discord.svg';
</script>

<a class="button" href="https://discord.gg/VwrK3jj" style="--font-size:{fontSize}em;--img-size:{fontSize*1.2}em">
    <img alt="Discord" class="discord" src={logo}/>&nbsp;Join my&nbsp;<span
        class="highlight">Discord</span>
</a>

<style>
    a {
        margin: 10px;
        padding: 10px;
        display: -webkit-flex;
        display: flex;
        align-items: center;
        /*    font-size: var(--font-size);*/
        /*    border-radius: 6px;*/
        /*    background-color: rgba(0, 0, 0, .3);*/
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