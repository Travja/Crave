<!--suppress CssUnresolvedCustomProperty -->
<script>
    export let size = 1.2;
</script>

<button on:click style="--size:{size}em;--x-size:{size/9}em">
    <span class="close"></span>
</button>

<style>
    button {
        position: absolute;
        top: 0;
        right: 0;
        margin: 10px;

        display: flex;
        justify-content: center;
        align-items: center;

        text-decoration: none;
        padding: 0px;
        color: white;
        background-color: red;
        border: none;
        border-radius: 50%;

        height: var(--size);
        width: var(--size);
    }

    button:hover {
        background-color: #b90000;
        cursor: pointer;
    }

    .close {
        position: relative;
        width: 100%;
        height: 60%;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .close:before, .close:after {
        position: absolute;
        content: ' ';
        height: 100%;
        width: var(--x-size);
        background-color: rgb(255, 255, 255);
    }

    .close:before {
        transform: rotate(45deg);
    }

    .close:after {
        transform: rotate(-45deg);
    }
</style>