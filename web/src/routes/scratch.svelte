<script>
    import {title} from "$lib/variables";
    import Cropper from "$lib/cropper/Cropper.svelte";

    title.set("Scratch");
</script>
<section>
    <Cropper/>
</section>
<style>
    section {
        text-align: center;
    }
</style>