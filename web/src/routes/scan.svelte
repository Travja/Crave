<script>
    import {title} from "$lib/variables";
    import Cropper from "$lib/cropper/Cropper.svelte";

    title.set("Scan");
</script>
<section>
    <Cropper/>
</section>
<style>
    section {
        text-align: center;
    }
</style>