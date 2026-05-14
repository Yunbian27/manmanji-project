export function useDevice() {
  const isMobile = useMediaQuery('(max-width: 767px)')
  const isTablet = useMediaQuery('(max-width: 1023px)')
  const isDesktop = useMediaQuery('(min-width: 1024px)')

  return { isMobile, isTablet, isDesktop }
}
