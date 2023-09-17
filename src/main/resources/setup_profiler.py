import cProfile

profiler = cProfile.Profile()


def profile(func):
    """Decorator for run function profile"""

    def wrapper(*args, **kwargs):
        return profiler.runcall(func, *args, **kwargs)

    return wrapper


def dump_profiler_stats(profile_filename: str):
    profiler.dump_stats(profile_filename)
